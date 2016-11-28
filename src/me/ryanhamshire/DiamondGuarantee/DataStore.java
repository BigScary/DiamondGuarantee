//Copyright 2015 Ryan Hamshire
package me.ryanhamshire.DiamondGuarantee;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataStore
{
    //in-memory cache for messages
    private String [] messages;
    
    private final static String dataLayerFolderPath = "plugins" + File.separator + "DiamondGuarantee";
    final static String playerDataFolderPath = dataLayerFolderPath + File.separator + "PlayerData";
    final static String messagesFilePath = dataLayerFolderPath + File.separator + "messages.yml";

    public DataStore()
	{
        //ensure data folders exist
        File playerDataFolder = new File(playerDataFolderPath);
        if(!playerDataFolder.exists())
        {
            playerDataFolder.mkdirs();
        }
        
        this.loadMessages();
	}
	
    private void loadMessages() 
    {
        Messages [] messageIDs = Messages.values();
        this.messages = new String[Messages.values().length];
        
        HashMap<String, CustomizableMessage> defaults = new HashMap<String, CustomizableMessage>();
        
        //initialize defaults
        this.addDefault(defaults, Messages.ReloadComplete, "Config reload complete.  If you've updated your JAR file, you'll have to /reload or reboot your server to activate the update.", null);
        this.addDefault(defaults, Messages.PlayerNotFound, "Player not found.", null);
        this.addDefault(defaults, Messages.DiamondScore, "Current Score: {0}", "0: score");
        this.addDefault(defaults, Messages.SetDiamondScore, "Score updated.", null);
        
        //load the config file
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(messagesFilePath));
        FileConfiguration outConfig = new YamlConfiguration();
        
        //for each message ID
        for(int i = 0; i < messageIDs.length; i++)
        {
            //get default for this message
            Messages messageID = messageIDs[i];
            CustomizableMessage messageData = defaults.get(messageID.name());
            
            //if default is missing, log an error and use some fake data for now so that the plugin can run
            if(messageData == null)
            {
                DiamondGuarantee.AddLogEntry("Missing message for " + messageID.name() + ".  Please contact the developer.");
                messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
            }
            
            //read the message from the file, use default if necessary
            this.messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
            outConfig.set("Messages." + messageID.name() + ".Text", this.messages[messageID.ordinal()]);
            
            if(messageData.notes != null)
            {
                messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
                outConfig.set("Messages." + messageID.name() + ".Notes", messageData.notes);
            }
        }
        
        //save any changes
        try
        {
            outConfig.save(DataStore.messagesFilePath);
        }
        catch(IOException exception)
        {
            DiamondGuarantee.AddLogEntry("Unable to write to the configuration file at \"" + DataStore.messagesFilePath + "\"");
        }
        
        defaults.clear();
    }

    private void addDefault(HashMap<String, CustomizableMessage> defaults, Messages id, String text, String notes)
    {
        CustomizableMessage message = new CustomizableMessage(id, text, notes);
        defaults.put(id.name(), message);       
    }

    synchronized public String getMessage(Messages messageID, String... args)
    {
        String message = messages[messageID.ordinal()];
        
        for(int i = 0; i < args.length; i++)
        {
            String param = args[i];
            message = message.replace("{" + i + "}", param);
        }
        
        return message;     
    }
}

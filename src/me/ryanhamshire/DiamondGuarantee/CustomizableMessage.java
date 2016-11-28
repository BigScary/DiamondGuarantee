//Copyright 2015 Ryan Hamshire

package me.ryanhamshire.DiamondGuarantee;

public class CustomizableMessage 
{
	public Messages id;
	public String text;
	public String notes;
	
	public CustomizableMessage(Messages id, String text, String notes)
	{
		this.id = id;
		this.text = text;
		this.notes = notes;
	}
}
package net.workbook.mileage.support;

public class ListRow 
{
	private String header;
	private String item;
	
	public ListRow(String header,String item)
	{
		this.header = header;
		this.item = item;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
}

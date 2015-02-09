package net.workbook.mileage.support;

public class ListDoubleRow 
{
	String header;
	String time;
	String firstRow;
	String secondRow;
	
	public ListDoubleRow(String header,String time,String firstRow, String secondRow)
	{
		this.header = header;
		this.time = time;
		this.firstRow = firstRow;
		this.secondRow = secondRow;
	}

	public String getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(String firstRow) {
		this.firstRow = firstRow;
	}

	public String getSecondRow() {
		return secondRow;
	}

	public void setSecondRow(String secondRow) {
		this.secondRow = secondRow;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	
}

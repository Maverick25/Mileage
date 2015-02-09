package net.workbook.mileage.model;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class CarRegNo implements Parcelable
{
	private int recordId;
	private int empId;
	private Date fromDate;
	private String carName;
	private String carRegNo;
	private boolean isActive;
	
	public CarRegNo(int recordId,int empId,Date fromDate,String carName,String carRegNo,boolean isActive)
	{
		this.recordId = recordId;
		this.empId = empId;
		this.fromDate = fromDate;
		this.carName = carName;
		this.carRegNo = carRegNo;
		this.isActive = isActive;
	}
	
	public CarRegNo(Parcel in)
	{
		recordId = in.readInt();
		empId = in.readInt();
		
		int day = in.readInt();
		int month = in.readInt();
		int year = in.readInt();
		
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		fromDate = cal.getTime();
		
		carName = in.readString();
		carRegNo = in.readString();
		isActive = in.readByte() == 1;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public int getEmpId() {
		return empId;
	}

	public void setEmpId(int empId) {
		this.empId = empId;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public String getCarName() {
		return carName;
	}

	public void setCarName(String carName) {
		this.carName = carName;
	}

	public String getCarRegNo() {
		return carRegNo;
	}

	public void setCarRegNo(String carRegNo) {
		this.carRegNo = carRegNo;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public static Calendar dateToCalendar(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	@Override
	public String toString() {
		return recordId+" - "+carName+" : "+carRegNo;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(recordId);
		out.writeInt(empId);
		
		Calendar cal = dateToCalendar(fromDate);
		
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		
		out.writeInt(day);
		out.writeInt(month);
		out.writeInt(year);
		
		out.writeString(carName);
		out.writeString(carRegNo);
		out.writeByte((byte) (isActive ? 1 : 0));
	}
	
	public static final Parcelable.Creator<CarRegNo> CREATOR = new Parcelable.Creator<CarRegNo>() 
			{
				public CarRegNo createFromParcel(Parcel in)
				{
					return new CarRegNo(in);
				}
				public CarRegNo[] newArray(int size)
				{
					return new CarRegNo[size];
				}
			};
	
}

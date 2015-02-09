package net.workbook.mileage.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Creditor implements Parcelable
{
	private int empArpAccountId;
	private int empId;
	private int arpAccId;
	private boolean useMileage;
	private String arpAccNo;
	private String arpAccName;
	
	public Creditor(int empArpAccountId,int empId,int arpAccId,boolean useMileage,String arpAccNo,String arpAccName)
	{
		this.empArpAccountId = empArpAccountId;
		this.empId = empId;
		this.arpAccId = arpAccId;
		this.useMileage = useMileage;
		this.arpAccNo = arpAccNo;
		this.arpAccName = arpAccName;
	}
	public Creditor(Parcel in)
	{
		empArpAccountId = in.readInt();
		empId = in.readInt();
		arpAccId = in.readInt();
		useMileage = in.readByte() == 1;
		arpAccNo = in.readString();
		arpAccName = in.readString();
	}

	public int getEmpArpAccountId() {
		return empArpAccountId;
	}

	public void setEmpArpAccountId(int empArpAccountId) {
		this.empArpAccountId = empArpAccountId;
	}

	public int getEmpId() {
		return empId;
	}

	public void setEmpId(int empId) {
		this.empId = empId;
	}

	public int getArpAccId() {
		return arpAccId;
	}

	public void setArpAccId(int arpAccId) {
		this.arpAccId = arpAccId;
	}

	public boolean isUseMileage() {
		return useMileage;
	}

	public void setUseMileage(boolean useMileage) {
		this.useMileage = useMileage;
	}

	public String getArpAccNo() {
		return arpAccNo;
	}

	public void setArpAccNo(String arpAccNo) {
		this.arpAccNo = arpAccNo;
	}

	public String getArpAccName() {
		return arpAccName;
	}

	public void setArpAccName(String arpAccName) {
		this.arpAccName = arpAccName;
	}

	@Override
	public String toString() {
		return arpAccNo+" - "+arpAccName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(empArpAccountId);
		out.writeInt(empId);
		out.writeInt(arpAccId);
		out.writeByte((byte) (useMileage ? 1 : 0));
		out.writeString(arpAccNo);
		out.writeString(arpAccName);
	}
	
	public static final Parcelable.Creator<Creditor> CREATOR = new Parcelable.Creator<Creditor>() 
			{
				public Creditor createFromParcel(Parcel in)
				{
					return new Creditor(in);
				}
				public Creditor[] newArray(int size)
				{
					return new Creditor[size];
				}
			};
	
	
}

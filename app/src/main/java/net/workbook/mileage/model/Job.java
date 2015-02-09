package net.workbook.mileage.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Job implements Parcelable
{
	private String jobName,customerName;
	private int jobId,statusId;
	
	public Job(int jobId,String jobName,int statusId,String customerName)
	{
		this.jobId = jobId;
		this.jobName = jobName;
		this.statusId = statusId;
		this.customerName = customerName;
	}
	
	private Job (Parcel in)
	{
		jobId = in.readInt();
		jobName = in.readString();
		statusId = in.readInt();
		customerName = in.readString();
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(jobId);
		out.writeString(jobName);
		out.writeInt(statusId);
		out.writeString(customerName);
	}
	
	public static final Parcelable.Creator<Job> CREATOR = new Parcelable.Creator<Job>()
			{
				public Job createFromParcel(Parcel in)
				{
					return new Job(in);
				}
				public Job[] newArray(int size)
				{
					return new Job[size];
				}
			};

	@Override
	public String toString() {
		return jobId+" - "+jobName;
	}
	
	
}

package net.workbook.mileage.support;

import java.util.ArrayList;

import net.workbook.mileage.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DoubleAdapter extends BaseAdapter
{
	private ArrayList<ListDoubleRow> doubleRows;
	private LayoutInflater inflater;
	
	private class ViewHolder
	{
		TextView header;
		TextView time;
		TextView firstRow;
		TextView secondRow;
	}
	
	public DoubleAdapter(Context context,ArrayList<ListDoubleRow> doubleRows)
	{
		inflater = LayoutInflater.from(context);
		this.doubleRows = doubleRows;
	}
	
	@Override
	public int getCount() {
		return doubleRows.size();
	}

	@Override
	public Object getItem(int position) {
		return doubleRows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
		
			convertView = inflater.inflate(R.layout.row_double, null);
			
			holder.header = (TextView) convertView.findViewById(R.id.header);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.firstRow = (TextView) convertView.findViewById(R.id.firstLine);
			holder.secondRow = (TextView) convertView.findViewById(R.id.secondLine);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.header.setText(doubleRows.get(position).getHeader());
		holder.time.setText(doubleRows.get(position).getTime());
		holder.firstRow.setText(doubleRows.get(position).getFirstRow());
		holder.secondRow.setText(doubleRows.get(position).getSecondRow());
		
		return convertView;
	}

}

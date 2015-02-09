package net.workbook.mileage.support;

import java.util.ArrayList;

import net.workbook.mileage.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SingleAdapter extends BaseAdapter 
{
	private LayoutInflater inflater;
	private ArrayList<ListRow> rows;
	
	private class ViewHolder
	{
		TextView labels;
		TextView values;
	}
	
	public SingleAdapter(Context context, ArrayList<ListRow> rows)
	{
		inflater = LayoutInflater.from(context);
		this.rows = rows;
	}
	
	@Override
	public int getCount() 
	{
		return rows.size();
	}

	@Override
	public ListRow getItem(int position) 
	{
		return rows.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			
			convertView = inflater.inflate(R.layout.row_single, null);
			
			holder.labels = (TextView) convertView.findViewById(R.id.headers);
			holder.values = (TextView) convertView.findViewById(R.id.items);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.labels.setText(rows.get(position).getHeader());
		holder.values.setText(rows.get(position).getItem());
		
		return convertView;
	}
	
	
	
}

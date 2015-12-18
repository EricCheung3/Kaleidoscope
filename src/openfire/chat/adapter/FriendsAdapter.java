package openfire.chat.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.videolan.vlc.R;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/** Friends adapter. It is used to process/display list of user's friends*/
public class FriendsAdapter extends BaseAdapter {

//	private static XMPPConnection connection = null;
	
	private Activity context;
	private List<Map<String,String>> listMap = new ArrayList<Map<String,String>>();
    private static HashMap<Integer,Boolean> isSelected = new HashMap<Integer,Boolean>();
    
	public FriendsAdapter(Activity context, List<Map<String,String>> listMap){
		this.context = context;
		this.listMap = listMap;
	}
	
	@Override
	public int getCount() {
		return listMap.size();
	}

	@Override
	public Object getItem(int position) {
		return listMap.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup arg2) {

		if(view == null)
			view = View.inflate(context, R.layout.frienditem, null);
		
		TextView username = (TextView) view.findViewById(R.id.friend_username);
		TextView displayname = (TextView) view.findViewById(R.id.friend_displayname);
		CheckBox check = (CheckBox) view.findViewById(R.id.check_box);
		
		if(listMap.get(position).get("status").equals("online")){
			displayname.setTextColor(Color.GREEN);
			displayname.setText("online");
		}else
			displayname.setText("offline");
			

		username.setText(listMap.get(position).get("username"));
		//check.setChecked(checked);
		
		return view;
	}


	public HashMap<Integer,Boolean> getIsSelected(){
		return isSelected;
	}
	
	public static void setIsSelected(HashMap<Integer,Boolean> isSelected){
		FriendsAdapter.isSelected = isSelected;
	}
	
	private boolean isChecked;
	public boolean getChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}

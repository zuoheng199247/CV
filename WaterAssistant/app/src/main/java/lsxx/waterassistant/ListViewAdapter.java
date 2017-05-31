package lsxx.waterassistant;

import android.content.ClipData;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by zzh on 2017/5/10.
 */

public class ListViewAdapter extends BaseAdapter {

    private List<String> titles = null;
    private List<String> contents = null;
    private List<Integer> imageId = null;
    private List<Integer> baseId = null;

    private LayoutInflater myLayoutInflater = null; //LayoutInflater可将xml布局转换为View对象
    private Context context = null;
    private ImageButton.OnClickListener myImageButtonListener = null;
    private RelativeLayout.OnTouchListener myOnTouchListener=null;

    private ListViewAdapter mAdapter = this;

    private static class ViewHolder {//创建一个内部类ViewHolder，设置选项布局中的元素
        public ImageButton ibtnStyle = null;
        public TextView tvTitles = null;
        public TextView tvContents = null;
        public TextView tvId=null;
        public RelativeLayout ibtnDelete = null;
    }

    public void setItemImageButtonListener(ImageButton.OnClickListener myImageButtonListener) {
        this.myImageButtonListener = myImageButtonListener;
    }

    public void setItemRelativeLayoutListener(RelativeLayout.OnTouchListener myOnTouchListener) {
        this.myOnTouchListener = myOnTouchListener;
    }

    public ListViewAdapter(List<String> titles, List<String> contents,
                           List<Integer> imageId, List<Integer> baseId, Context context) {
        this.context = context;
        this.titles = titles;
        this.contents = contents;
        this.imageId = imageId;
        this.baseId = baseId;
        this.myLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int count = 0;
        if (imageId == null || titles == null || contents == null) {
            return count;
        } else {
            return titles.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = myLayoutInflater.inflate(R.layout.list_item, null);
            holder.ibtnStyle = (ImageButton) convertView.findViewById(R.id.ibtnStyle);
            holder.tvTitles = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvContents = (TextView) convertView.findViewById(R.id.tvContent);
            holder.ibtnDelete = (RelativeLayout) convertView.findViewById(R.id.delete_button);
            holder.tvId=(TextView) convertView.findViewById(R.id.tvId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ibtnStyle.setImageResource(imageId.get(position));
        holder.ibtnStyle.setOnClickListener(myImageButtonListener);
        holder.ibtnDelete.setOnTouchListener(myOnTouchListener);
//        holder.ibtnDelete.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    int id = baseId.get(position);
//                    MyDatabaseHelper dbHelper = new MyDatabaseHelper(context, "myRecord.db3", 1);
//
//                    dbHelper.deleteDate(id, "", "", 0);
//                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
//                    dbHelper.close();
//                    titles.remove(position);
//                    contents.remove(position);
//                    imageId.remove(position);
//                    baseId.remove(position);
//
////                    Toast.makeText(context, "已删除" + titles.size() + contents.size() + imageId.size() + baseId.size(), Toast.LENGTH_SHORT).show();
//                    mAdapter.notifyDataSetChanged();
//
//                }
//                return false;
//            }
//        });
        holder.tvTitles.setText(titles.get(position).toString());
        holder.tvContents.setText(contents.get(position).toString());
        holder.tvContents.setMovementMethod(new ScrollingMovementMethod());
        holder.tvId.setText(baseId.get(position).toString());
        return convertView;
    }
}

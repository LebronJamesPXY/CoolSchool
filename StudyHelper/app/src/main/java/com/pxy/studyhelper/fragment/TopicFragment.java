package com.pxy.studyhelper.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pxy.studyhelper.R;
import com.pxy.studyhelper.adapter.TopicAdapter;
import com.pxy.studyhelper.biz.GetTopicBiz;
import com.pxy.studyhelper.entity.Topic;
import com.pxy.studyhelper.utils.DialogUtil;
import com.pxy.studyhelper.utils.Tools;

import org.xutils.common.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;

/**
 * 展示所有人动态fragemnt
 */
public class TopicFragment extends Fragment {

    private PullToRefreshListView   mListView;
    private TopicAdapter  mTopicAdapter;
    private GetTopicBiz  mGetTopicBiz;
    public static LinkedList<Topic> mTopicList=new LinkedList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_topic,null);

        setListView(view);

        DialogUtil.showProgressDialog(getActivity(), "正在拼命加载数据...");
        queryData(0, STATE_REFRESH);
        return view;
    }

    private void setListView(View view) {
        mListView= (PullToRefreshListView) view.findViewById(R.id.listView);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        //刷新时可以滚动listView
        mListView.setScrollingWhileRefreshingEnabled(true);
//        mListView.setShowIndicator(true);
        mListView.setShowViewWhileRefreshing(true);
        ILoadingLayout startLabels = mListView.getLoadingLayoutProxy(true, true);
        startLabels.setPullLabel("下拉刷新...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在载入...");// 刷新时
        startLabels.setReleaseLabel("松开刷新...");// 下拉达到一定距离时，显示的提示

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 下拉刷新(从第一页开始装载数据)
                queryData(0, STATE_REFRESH);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 上拉加载更多(加载下一页数据)
                queryData(curPage, STATE_MORE);
            }
        });
//        ListView mMsgListView = mListView.getRefreshableView();
//        // 再设置adapter
//        mMsgListView.setAdapter(new TopicAdapter(getActivity(), mTopicList));
//        mTopicAdapter.notifyDataSetChanged();
    }
    private  void updateListView(){
        if(mTopicAdapter==null) {
            mTopicAdapter = new TopicAdapter(getActivity(), mTopicList);
            mListView.setAdapter(mTopicAdapter);
        }else{
            mTopicAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTopicAdapter!=null){
            mTopicAdapter.notifyDataSetChanged();
        }
    }

    //
//    public void updateListView(List<Topic> list) {
//        if(mTopicAdapter==null) {
//            mTopicList.addAll(list);
//            mTopicAdapter = new TopicAdapter(getActivity(), mTopicList);
//            mListView.setAdapter(mTopicAdapter);
//        }else{
//            mTopicAdapter.notifyDataSetChanged();
//            mListView.onRefreshComplete();
//        }
//        DialogUtil.closeProgressDialog();
//    }

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多

    private int limit = 4;		//每次查询个数  每页显示个数
    private int curPage =0;		// 当前页的编号，从0开始
    private String  lastTime="2016-02-15 22:56:18";   //最后动态时间
    private String  firstTime="2016-02-15 22:56:18";   //最新动态时间

    /**
     * 分页获取数据
     * @param page	页码
     * @param actionType	ListView的操作类型（下拉刷新、上拉加载更多）
     */
    private void queryData( int page, final int actionType){
        Log.i("bmob", "pageN:" + page + " limit:" + limit + " actionType:" + actionType);

        BmobQuery<Topic> query = new BmobQuery<>();
        // 按时间降序查询
        query.order("-createdAt");
        query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        // 如果是加载更多
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(actionType == STATE_MORE){
            // 处理时间查询
            try {
                date = sdf.parse(lastTime);
                LogUtil.i("last time---date  to  string---"+date.toString());
            } catch (Exception e) {
                LogUtil.e(e.getMessage());
            }
            // 只查询小于等于最后一个item发表时间的数据
            query.addWhereLessThan("createdAt", new BmobDate(date));
            // 跳过之前页数并去掉重复数据
            query.setSkip(page * limit);
        }else{
            // 处理时间查询
            try {
                date = sdf.parse(firstTime);
                LogUtil.i("first time---date  to  string---"+date.toString());
                query.addWhereGreaterThan("createdAt", new BmobDate(date));
            } catch (Exception e) {
                LogUtil.e(e.getMessage());
            }
            // 只查询小于等于最后一个item发表时间的数据
            page=0;
            query.setSkip(page);
        }
        // 设置每页数据个数
        query.setLimit(limit);
        // 查找数据
        query.findObjects(getActivity(), new FindListener<Topic>() {
            @Override
            public void onSuccess(List<Topic> list) {
                if(list.size()>0){
                    if(actionType == STATE_REFRESH){
                        if(list.size()==1){
                            mListView.onRefreshComplete();
                            Tools.ToastShort("没有最新数据了...");
                            return;
                        }
                        // 当是下拉刷新操作时，将当前页的编号重置为0，
                        if(mTopicList.size()>0&&mTopicList.getFirst().getContent().equals(list.get(list.size()-1).getContent())){
                            for (int i=list.size()-2;i>=0;i--) {
                                mTopicList.addFirst(list.get(i));
                                LogUtil.i("STATE_REFRESH--666---222222-" + list.get(i).toString());
                            }
                        }else if(mTopicList.size()>0&&list.size()>=2&&mTopicList.getFirst().getContent().equals(list.get(list.size()-2).getContent())){
                            for (int i=list.size()-3;i>=0;i--) {
                                mTopicList.addFirst(list.get(i));
                                LogUtil.i("STATE_REFRESH--666---????-" + list.get(i).toString());
                            }
                        }else{
                            for (int i=list.size()-1;i>=0;i--) {
                                LogUtil.i("STATE_REFRESH--666----" + list.get(i).toString());
                                mTopicList.addFirst(list.get(i));
                            }
                        }
                        // 获取动态时间
                        firstTime=list.get(0).getCreatedAt();
                        lastTime = mTopicList.getLast().getCreatedAt();
                        LogUtil.i("first time---666---"+firstTime);
                        LogUtil.i("last time---666---"+lastTime);
                    }else {
                        // 将本次查询的数据添加到mTopicList中
                        for (Topic td : list) {
                            LogUtil.i("STATE_more--666--"+td.toString());
                            mTopicList.addLast(td);
                        }
                        curPage++;
                    }
                    updateListView();
//                    Tools.ToastShort("第"+curPage+"页数据加载完成");
                    LogUtil.i("666---第"+curPage+"页数据加载完成");
                }else if(actionType == STATE_MORE){
                    Tools.ToastShort("没有更多数据了");
                }else if(actionType == STATE_REFRESH){
                    Tools.ToastShort("没有最新数据了");
                }
                DialogUtil.closeProgressDialog();
                mListView.onRefreshComplete();
            }

            @Override
            public void onError(int arg0, String arg1) {
                DialogUtil.closeProgressDialog();
                LogUtil.e(arg0+"查询动态数据失败:"+arg1);
                Tools.ToastShort("获取数据失败,请检查网络");
                mListView.onRefreshComplete();
            }
        });
    }

}

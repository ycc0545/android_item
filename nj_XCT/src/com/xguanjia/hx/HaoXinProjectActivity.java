package com.xguanjia.hx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.LOG;
import org.jivesoftware.smack.ChatManager;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cmcc.ueprob.agent.UEProbAgent;
import cn.sharesdk.framework.authorize.e;

import com.chinamobile.salary.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoqee.chat.ChatDetailActivity;
import com.haoqee.chat.ChatMainActivity;
import com.haoqee.chat.GroupDetailActivity;
import com.haoqee.chat.NotifyActivity;
import com.haoqee.chat.UserInfoActivity;
import com.haoqee.chat.Fragment.FriendsFragment;
import com.haoqee.chat.entity.NotifyType;
import com.haoqee.chat.global.Common;
import com.haoqee.chat.global.FeatureFunction;
import com.haoqee.chatsdk.TCChatManager;
import com.haoqee.chatsdk.Interface.LoginListenser;
import com.haoqee.chatsdk.Interface.TCMessageListener;
import com.haoqee.chatsdk.Interface.TCNotifyListener;
import com.haoqee.chatsdk.Interface.TCNotifyMessageListener;
import com.haoqee.chatsdk.entity.ChatType;
import com.haoqee.chatsdk.entity.TCError;
import com.haoqee.chatsdk.entity.TCGroup;
import com.haoqee.chatsdk.entity.TCMessage;
import com.haoqee.chatsdk.entity.TCMessageType;
import com.haoqee.chatsdk.entity.TCNotifyType;
import com.haoqee.chatsdk.entity.TCNotifyVo;
import com.haoqee.chatsdk.entity.TCSession;
import com.xguanjia.hx.badges.ABadgeUtil;
import com.xguanjia.hx.common.ActionResponse;
import com.xguanjia.hx.common.AppUtils;
import com.xguanjia.hx.common.Constants;
import com.xguanjia.hx.common.DatabaseHelper;
import com.xguanjia.hx.common.HaoqeeException;
import com.xguanjia.hx.common.ServerAdaptor;
import com.xguanjia.hx.common.ServiceSyncListener;
import com.xguanjia.hx.common.activity.BaseActivity;
import com.xguanjia.hx.common.util.MyDimensAdapter;
import com.xguanjia.hx.contact.bean.PersonBean;
import com.xguanjia.hx.contact.bean.TimestampBean;
import com.xguanjia.hx.contact.service.TimestampDb;
import com.xguanjia.hx.haoxinchat.ChatActivity;
import com.xguanjia.hx.haoxinchat.GroupChatActivity;
import com.xguanjia.hx.login.activity.TeLoginActivity;
import com.xguanjia.hx.login.activity.TelListener;
import com.xguanjia.hx.login.update.UpdateManager;
import com.xguanjia.hx.notice.NoticeActivity;
import com.xguanjia.hx.notice.db.NoticeDb;
import com.xguanjia.hx.openfire.bean.OpenfireMessageBean;
import com.xguanjia.hx.openfire.fianlconstant.CommonConstant;
import com.xguanjia.hx.openfire.listener.XMPPChatListenerService;
import com.xguanjia.hx.openfire.listener.XMPPFileListenerService;
import com.xguanjia.hx.openfire.listener.XmppTool;
import com.xguanjia.hx.payroll.activity.HomeActivity;
import com.xguanjia.hx.payroll.activity.VP_jiekou;
import com.xguanjia.hx.payroll.activity.ViewpagerActivity;
import com.xguanjia.hx.payroll.bean.PayRollTypeGroup;
import com.xguanjia.hx.payroll.bean.PayRollTypeGroupList;
import com.xguanjia.hx.payroll.db.PayRollDb;
import com.xguanjia.hx.set.SetGroupActivity;

/**
 * 主界面
 * 
 * 
 */
public class HaoXinProjectActivity extends BaseActivity implements
		OnCheckedChangeListener {
	public static ChatManager cm;
	private static final String TAG = "HaoXinProjectActivity";
	private RadioGroup bottomRadioGroup;
	private ImageView bottomFrontBgTv;
	private int bottomStartLeft;
	private LayoutParams params;
	private Intent intent, intent1;
	private View view;
	private TextView remindTv_my, remindTv_sr;
	private String resMessage = "";
	private long exitTime = 0;
	private int downLoadCount = 0;
	private long totalCount = 0;// 总记录数
	private SharedPreferences sf1;

	private boolean isExit = false;

	private HashMap<String, Object> requestMap;

	private TypeToken<List<PersonBean>> targetType;

	private Message msg;

	private String requestMethod = Constants.UrlHead
			+ "client.action.ContactsAction$getOrganization";

	private String contactCountMethod = Constants.UrlHead
			+ "client.action.ContactsAction$getOrganizationSize";

	private TimestampBean m_timestampBean = new TimestampBean();

	private TimestampDb m_timestampDb;

	private ProgressDialog pd;

	private RefreshRemind refreshRemind;
	private Context mContext;
	// 查询未读数
	private PayRollDb rollDb;
	private NoticeDb noticeDb;
	// private int unPcount=0;
	private String tag_String = "";// 用来判断工资，或我的 上的小红点

	public static String user_yan_frag = "";// 为fragment 判断是否为演示账号

	public static String dangqian_view = "1";// 若为1,则在首页，若为2，则为收入页，若为3，则为我的页

	/** 监听网络状态通知 */
	public final static String ACTION_NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	/** 被挤下线或者是未登录时显示TOAST通知 */
	public static final String ACTION_SHOW_TOAST = "com.xizue.thinkchat.intent.action.ACTION_SHOW_TOAST";
	/** 注销 */
	public static final String ACTION_LOGIN_OUT = "com.xizue.thinkchat.intent.action.ACTION_LOGIN_OUT";
	/** 登录成功执行操作通知 */
	public final static String LOGIN_SUCCESS_ACTION = "com.xizue.thinkchat.action.LOGIN_SUCCESS_ACTION";
	/** 退出通知 */
	public final static String SYSTEM_EXIT = "com.xizue.thinkchat.action.SYSTEM_EXIT";
	/** 被踢出临时会话或群主删除临时会话通知 */
	public final static String ACTION_KICK_OR_DELETE_TEMP_CHAT = "com.xizue.thinkchat.action.ACTION_KICK_OR_DELETE_TEMP_CHAT";

	/** 更新未读数通知 */
	public final static String UPDATE_SESSION_UNREAD_COUNT = "com.xizue.thinkchat.action.UPDATE_SESSION_UNREAD_COUNT";

	/** 发送文件通知 */
	public final static String ACTION_SEND_FILE_MESSAGE = "com.xizue.thinkchat.action.ACTION_SEND_FILE_MESSAGE";
	/** 下载文件通知 */
	public final static String ACTION_DOWNLOAD_FILE_MESSAGE = "com.xizue.thinkchat.action.ACTION_DOWNLOAD_FILE_MESSAGE";
	private boolean mIsRegisterReceiver = false;
	public final static int SHOW_PROGRESS_DIALOG = 11112;
	public final static int HIDE_PROGRESS_DIALOG = 11113;
	public final static int UPDATE_LIST_DATA = 11114;
	public final static int MSG_NETWORK_ERROR = 11306;
	public final static int MSG_TICE_OUT_EXCEPTION = 11307;
	public final static int MSG_LOAD_ERROR = 11818;
	public final static int LIST_LOAD_FIRST = 501;
	public final static int LIST_LOAD_REFERSH = 502;
	public final static int LIST_LOAD_MORE = 503;
	public final static int SHOW_LOADINGMORE_INDECATOR = 11105;
	public final static int HIDE_LOADINGMORE_INDECATOR = 11106;
	public final static int LOGIN_REQUEST = 29312;
	public final static int SHOW_SCROLLREFRESH = 11117;
	public final static int HIDE_SCROLLREFRESH = 11118;
	/** 通知栏中通知的ID */
	public static final int NOTION_ID = 1000000023;

	public static boolean first_login = false;

	public static boolean first_enter_setActivity = false;
	public static boolean kaishi = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("mtagtag", "onCreate");
		first_login = true;
		Log.d(TAG, "in onCreate method");
		sf1 = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		sp = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE);
		sp.edit()
				.putString(
						"headImg",
						"http://" + sf1.getString("ip", Constants.IP)
								+ Constants.loginBean.getHeadImg()).commit();
		user_yan_frag = sp.getString("userName_yanshi", "");
		mContext = this;
		vp = new VP_jiekou(mContext);
		registerNetWorkMonitor();
		Log.d(TAG, "in onCreate method");
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		TelListener myPhoneCallListener = new TelListener(this);
		tm.listen(myPhoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
		init();
		if (sp.getString("userName_yanshi", "").equals("13911111122")) {
			ABadgeUtil.setBadge(HaoXinProjectActivity.this, 0);

		} else {
			ABadgeUtil.setBadge(HaoXinProjectActivity.this,
					Constants.unPayrollNum + Constants.unnoticNum);
		}

	}

	private void init() {

		String logisticServer = Constants.OF_LOGISTIC_SERVER;
		TCChatManager.getInstance().setLogitiscServer(logisticServer);
		rollDb = new PayRollDb(this);
		noticeDb = new NoticeDb(this);
		Constants.unPayrollNum = rollDb.getUnreadNum();
		Constants.unnoticNum = noticeDb.getUnnoticeNum();
		// 登录成功，发通知更新XMPP
		mContext.sendBroadcast(new Intent(LOGIN_SUCCESS_ACTION));
		initViews();
	}

	/**
	 * 界面初始化
	 */
	private void initViews() {

		bottomView.removeAllViews();
		View v = getLayoutInflater().inflate(
				R.layout.radiogroup_bottom_main_lc, null);
		bottomView.addView(v);
		bottomRadioGroup = (RadioGroup) bottomView
				.findViewById(R.id.bottom_radio_group);
		// 红色消息提醒图标
		remindTv_sr = (TextView) bottomView
				.findViewById(R.id.chat_message_num_sr);
		remindTv_my = (TextView) bottomView.findViewById(R.id.chat_message_num);
		// if (Constants.unPayrollNum != 0) {
		// remindTv_sr.setVisibility(View.VISIBLE);
		// }
		// if (Constants.unnoticNum != 0) {
		// remindTv_my.setVisibility(View.VISIBLE);
		// }

		bottomRadioGroup.setVisibility(View.VISIBLE);
		bottomRadioGroup.setOnCheckedChangeListener(this);
		// 底部前景色
		bottomFrontBgTv = new ImageView(this);
		// bottomFrontBgTv.setImageResource(R.drawable.tab_front_bg);
		bottomView.addView(bottomFrontBgTv);
		view = new View(HaoXinProjectActivity.this);
		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		Constants.DEVICE_IMEI = tm.getDeviceId();
		Constants.DEVICE_IMSI = tm.getSubscriberId();

		targetType = new TypeToken<List<PersonBean>>() {
		};
		requestMap = new HashMap<String, Object>();

		m_timestampDb = new TimestampDb(getApplicationContext());

		// 消息提示广播
		refreshRemind = new RefreshRemind();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("RefreshRemind");
		registerReceiver(refreshRemind, intentFilter);

		zg_broad = new Xhd_gzgg_and_my_Broadcast();
		IntentFilter intent_fil = new IntentFilter();
		intent_fil.addAction("com.myandgz.xhd");
		registerReceiver(zg_broad, intent_fil);

		// openfire new OpenfireBroadcastReceiver

		openfire_msg = new OpenfireBroadcastReceiver();
		IntentFilter intent_fi2 = new IntentFilter();
		intent_fi2.addAction(CommonConstant.OPENFIREMSG_BRODACAST);
		registerReceiver(openfire_msg, intent_fi2);

		// 获取工资单的类型
		getPayRollType();

	}

	private Xhd_gzgg_and_my_Broadcast zg_broad;

	private class Xhd_gzgg_and_my_Broadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			remindTv_my.setVisibility(View.GONE);
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		Log.i("mtagtag", "onWindowFocusChanged");
		MyDimensAdapter.getPhoneSecrren(this);
	}

	/**
	 * 获取工资单（收入）的类型
	 */
	private void getPayRollType() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("partyId", Constants.partyId);
		map.put("version", getVersion());
		try {
			ServerAdaptor.getInstance(this).doAction(
					1,
					Constants.UrlHead
							+ "client.action.SalaryAction$getSalaryUseType",
					requestMap, new ServiceSyncListener() {

						public void onSuccess(ActionResponse returnObject) {
							// TODO Auto-generated method stub
							if (pd != null && pd.isShowing()) {
								pd.dismiss();
							}
							Constants.payRollTypeGroups = new Gson().fromJson(
									returnObject.getData().toString(),
									new TypeToken<PayRollTypeGroupList>() {
									}.getType());

							for (int i = 0; i < Constants.payRollTypeGroups
									.getSalaryUseTypes().size(); i++) {
								PayRollTypeGroup payRollTypeGroup1 = Constants.payRollTypeGroups
										.getSalaryUseTypes().get(i);
								if ("福利补贴".equals(payRollTypeGroup1
										.getUseGroupingName())) {
									payRollTypeGroup1.setUseGroupingName("福利");
									Constants.payRollTypeGroups
											.getSalaryUseTypes().set(i,
													payRollTypeGroup1);
								}
							}
							PayRollTypeGroup payRollTypeGroup = new PayRollTypeGroup();
							payRollTypeGroup.setUseGroupingId("0");
							payRollTypeGroup.setUseGroupingName("全部");

							Constants.payRollTypeGroups.getSalaryUseTypes()
									.add(0, payRollTypeGroup);
						}

						public void onError(ActionResponse returnObject) {
							// TODO Auto-generated method stub
							if (pd != null && pd.isShowing()) {
								pd.dismiss();
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// radioGroup选择事件
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		// 好信 薪酬通首页
		case R.id.radio_haoxin:
			AppUtils.SetImageSlide(bottomFrontBgTv, bottomStartLeft, 0, 0, 0);
			bottomStartLeft = 0;
			intent = new Intent(HaoXinProjectActivity.this, HomeActivity.class);
			view = getLocalActivityManager().startActivity("chat", intent)
					.getDecorView();
			break;
		// 通讯录 薪酬通收入
		case R.id.radio_contact:
			sp.edit().putBoolean("isread_sr", false).commit();
			remindTv_sr.setVisibility(View.GONE);
			AppUtils.SetImageSlide(bottomFrontBgTv, bottomStartLeft,
					bottomFrontBgTv.getWidth(), 0, 0);
			bottomStartLeft = bottomFrontBgTv.getWidth();
			Log.i(TAG, "普通模式");
			intent = new Intent(HaoXinProjectActivity.this,
					ViewpagerActivity.class);
			view = getLocalActivityManager().startActivity("contact", intent)
					.getDecorView();
			break;
		// 设置 薪酬通我的
		case R.id.radio_set:
			// 保存 通知状态是否已读
			sp.edit().putBoolean("isread_my", false).commit();
			remindTv_my.setVisibility(View.GONE);
			AppUtils.SetImageSlide(bottomFrontBgTv, bottomStartLeft,
					bottomFrontBgTv.getWidth() * 3, 0, 0);
			bottomStartLeft = bottomFrontBgTv.getWidth() * 3;
			intent = new Intent(HaoXinProjectActivity.this,
					SetGroupActivity.class);
			view = getLocalActivityManager().startActivity("set", intent)
					.getDecorView();
			break;
		default:
			break;
		}
		mainView.removeAllViews();
		mainView.addView(view, params);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "触发onKeyDown方法。。。。。。。。。。。。");

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (!isExit) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				isExit = true;
				return true;
			} else {

				if (sp.getString("userName_yanshi", "").equals("13911111122")) {
					ABadgeUtil.setBadge(HaoXinProjectActivity.this, 0);

				} else {
					ABadgeUtil.setBadge(HaoXinProjectActivity.this,
							Constants.unPayrollNum + Constants.unnoticNum);
				}

				Editor ed1 = sp.edit();
				ed1.remove("userName_yanshi");
				ed1.commit();

				Intent intent = new Intent(HaoXinProjectActivity.this,
						TeLoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle bundle = new Bundle();
				bundle.putString("AppExit", "app_exit");
				intent.putExtras(bundle);
				startActivity(intent);
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		UEProbAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i("mtagtag", "onResume");
		super.onResume();
		UEProbAgent.onResume(this);
	}

	/**
	 * 广播回调
	 */

	private class RefreshRemind extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("RefreshRemind")) {
				int remindNum = intent.getIntExtra("num", 0);
				if (remindNum != 0) {
					remindTv_my.setVisibility(View.VISIBLE);
					remindTv_my.setText(remindNum + "");
				} else {
					remindTv_my.setVisibility(View.GONE);
				}

			} else if (intent.getAction().equals("RefreshContact")) {
				m_timestampBean = m_timestampDb.selectTimestamp();
				if ("0".equals(m_timestampBean.getDepartmentTimestamp())
						&& "0".equals(m_timestampBean.getPersonTimestamp())) {
					// initContactData();
				}
			}

		}
	}

	// 登陆监听
	private LoginListenser mLoginListenser = new LoginListenser() {

		@Override
		public void onSuccess() {
			// 登录XMPP成功之后进入该回调
			// 注册消息监听事件
			TCChatManager.getInstance().setNotifyMessageListener(
					mTCNotifyMessageListener);
			TCChatManager.getInstance().setNotifyListener(mNotifyListener);
		}

		@Override
		public void onFailed(String error) {
			// 登录XMPP失败之后进入该回调
		}

		@Override
		public void onConflict() {
			// 账号被挤下线之后进入该回调
			if (sp.contains("userName_yanshi")) {
				if (sp.getString("userName_yanshi", "").equals("13911111122")) {

					return;
				}

			}
			if (!Constants.loginName.equals("13911111122")) {
				Intent toastIntent = new Intent(
						HaoXinProjectActivity.ACTION_SHOW_TOAST);
				toastIntent.putExtra("toast_msg",
						mContext.getString(R.string.openfire_login_prompt));
				mContext.sendBroadcast(toastIntent);
				Common.saveLoginResult(mContext, null);
				Common.setUid("");
				Common.setToken("");
				TCChatManager.getInstance().logoutXmpp();

			}
		}
	};

	private VP_jiekou vp;

	/**
	 * new
	 * **/
	private OpenfireBroadcastReceiver openfire_msg;

	private class OpenfireBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			OpenfireMessageBean msgBean = (OpenfireMessageBean) intent
					.getSerializableExtra("data");
			if (msgBean == null) {
				return;
			}
			String msg = null;
			msg = msgBean.content;// 内容
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher; // 设置通知的图标
			long currentTime = System.currentTimeMillis();
			if (currentTime - Common.getNotificationTime(mContext) > Common.NOTIFICATION_INTERVAL) {
				if (Common.getOpenSound(mContext)) {
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				Common.saveNotificationTime(mContext, currentTime);
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}

			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// 音频将被重复直到通知取消或通知窗口打开。
			// notification.flags |= Notification.FLAG_INSISTENT;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.when = currentTime;

			// 若为通知公告则为tzgg，若为收入则为20150702095042这样的时间
			tag_String = msgBean.type;
			int unreadSum = 0;
			if (tag_String.equals("tzgg")) {// 通知公告
				sp.edit().putBoolean("isread_my", true).commit();
				intent1 = new Intent(mContext, NoticeActivity.class);

				// 在应用图标显示未读数字
				Constants.unnoticNum = noticeDb.getUnnoticeNum() + 1;
				Constants.unPayrollNum = rollDb.getUnreadNum();
				// 用于设置 通知公告栏的小红点
				mContext.sendBroadcast(new Intent("com.tzgg.xhd"));
				mContext.sendBroadcast(new Intent("com.home.tzgg.xhd"));
			} else { // 收入的
				sp.edit().putBoolean("isread_sr", true).commit();
				Intent intentBroadcast = new Intent();
				intentBroadcast.setAction("homepayroll");
				sendBroadcast(intentBroadcast);
				intent1 = new Intent(mContext, HaoXinProjectActivity.class);
				intent1.putExtra("type", "2");
				Constants.payRollRefresh = true;
				// 在应用图标显示未读数字
				Constants.unPayrollNum = rollDb.getUnreadNum() + 1;
				Constants.unnoticNum = noticeDb.getUnnoticeNum();
			}

			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));
			unreadSum = Constants.unnoticNum + Constants.unPayrollNum;
			if (sp.getString("userName_yanshi", "").equals("13911111122")) {
				ABadgeUtil.setBadge(HaoXinProjectActivity.this, 0);

			} else {
				ABadgeUtil.setBadge(HaoXinProjectActivity.this, unreadSum);
			}

			PendingIntent contentIntent = PendingIntent.getActivity(mContext,
					1000, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(mContext, "薪酬通提醒", msg,
					contentIntent);
			NotificationManager manager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);

			manager.notify(1000, notification);

		}
	};

	/**
	 * new
	 * **/

	// 收到消息之后进入该回调
	public TCNotifyMessageListener mTCNotifyMessageListener = new TCNotifyMessageListener() {

		@Override
		public void doComplete(TCMessage message) {

			String msg = null;

			switch (message.getMessageType()) {
			case TCMessageType.PICTURE:
				msg = " <" + mContext.getString(R.string.get_picture) + " > ";
				break;
			case TCMessageType.TEXT:
				msg = message.getTextMessageBody().getContent();
				break;
			case TCMessageType.VOICE:
				msg = " <" + mContext.getString(R.string.get_voice) + " > ";
				break;
			case TCMessageType.MAP:
				msg = " <" + mContext.getString(R.string.get_location) + " > ";
				break;

			default:
				break;
			}

			// Notification
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher; // 设置通知的图标
			long currentTime = System.currentTimeMillis();
			if (currentTime - Common.getNotificationTime(mContext) > Common.NOTIFICATION_INTERVAL) {
				if (Common.getOpenSound(mContext)) {
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
				Common.saveNotificationTime(mContext, currentTime);
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}

			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// 音频将被重复直到通知取消或通知窗口打开。
			// notification.flags |= Notification.FLAG_INSISTENT;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.when = currentTime;

			TCSession session = null;
			if (message.getChatType() == ChatType.SINGLE_CHAT) {
				session = TCChatManager.getInstance().getSessionByID(
						message.getFromId(), ChatType.SINGLE_CHAT);
			} else {
				session = TCChatManager.getInstance().getSessionByID(
						message.getToId(), message.getChatType());
			}

			try {
				ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				RunningTaskInfo info = activityManager.getRunningTasks(1)
						.get(0);
				String shortClassName = info.topActivity.getShortClassName(); // 类名
				if (shortClassName
						.equals("com.xizue.thinkchat.ChatMainActivity")
						&& Common.getChatType(mContext) == message
								.getChatType()) {
					if (FeatureFunction.isAppOnForeground(mContext)) {
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!Common.getAcceptMsgAuth(mContext)) {
				return;
			}

			if (message.getChatType() != ChatType.SINGLE_CHAT) {
				TCGroup group = TCChatManager.getInstance().queryGroupByID(
						message.getToId(), message.getChatType());
				if (group != null && group.getGroupGetMsg() == 0) {
					return;
				}
			}

			String id = "";
			if (message.getChatType() == ChatType.SINGLE_CHAT) {

				id = message.getFromId();
			} else {

				id = message.getToId();
			}

			// 若为通知公告则为tzgg，若为收入则为20150702095042这样的时间
			tag_String = message.getMessageTag();
			int unreadSum = 0;
			if (tag_String.equals("tzgg")) {// 通知公告
				sp.edit().putBoolean("isread_my", true).commit();
				intent1 = new Intent(mContext, NoticeActivity.class);
				intent1.putExtra("session", session);
				// vp.getNoticeList();
				// 在应用图标显示未读数字
				Constants.unnoticNum = noticeDb.getUnnoticeNum() + 1;
				Constants.unPayrollNum = rollDb.getUnreadNum();
				// 用于设置 通知公告栏的小红点
				mContext.sendBroadcast(new Intent("com.tzgg.xhd"));
				mContext.sendBroadcast(new Intent("com.home.tzgg.xhd"));
			} else { // 收入的
				sp.edit().putBoolean("isread_sr", true).commit();
				Intent intentBroadcast = new Intent();
				intentBroadcast.setAction("homepayroll");
				sendBroadcast(intentBroadcast);
				intent1 = new Intent(mContext, HaoXinProjectActivity.class);
				intent1.putExtra("type", "2");
				Constants.payRollRefresh = true;
				intent1.putExtra("session", session);
				// vp.doAsyncJsonAction();
				// 在应用图标显示未读数字
				Constants.unPayrollNum = rollDb.getUnreadNum() + 1;
				Constants.unnoticNum = noticeDb.getUnnoticeNum();
			}

			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));
			unreadSum = Constants.unnoticNum + Constants.unPayrollNum;
			if (sp.getString("userName_yanshi", "").equals("13911111122")) {
				ABadgeUtil.setBadge(HaoXinProjectActivity.this, 0);

			} else {
				ABadgeUtil.setBadge(HaoXinProjectActivity.this, unreadSum);
			}

			PendingIntent contentIntent = PendingIntent.getActivity(mContext,
					id.hashCode(), intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(mContext, "薪酬通提醒", msg,
					contentIntent);
			NotificationManager manager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);

			manager.notify(id.hashCode(), notification);

		}
	};

	private TCNotifyListener mNotifyListener = new TCNotifyListener() {

		@Override
		public void receive(TCNotifyVo notify) {
			// 收到通知后进入该回调
			sendNotify(notify);
		}
	};

	/**
	 * 发送通知到系统通知栏
	 * 
	 * 通知对象
	 */
	private void sendNotify(TCNotifyVo notifyVo) {

		sendBroadcast(new Intent(ChatActivity.UPDATE_COUNT_ACTION));

		String msg = "";
		msg = notifyVo.getContent();
		switch (notifyVo.getType()) {
		case NotifyType.SYSTEM_MSG:
			msg = mContext.getString(R.string.system_info);
			break;

		case NotifyType.APPLY_FRIEND:
			msg = notifyVo.getUser().getName()
					+ mContext.getString(R.string.apply_add_friend_with_you);
			break;

		case NotifyType.AGREE_ADD_FRIEND:
			msg = notifyVo.getUser().getName()
					+ mContext.getString(R.string.agree_add_friend_with_you);
			Intent addFriendIntent = new Intent(
					UserInfoActivity.ACTION_AGREE_FRIEND);
			addFriendIntent.putExtra("uid", notifyVo.getUser().getUserID());
			sendBroadcast(addFriendIntent);
			mContext.sendBroadcast(new Intent(
					FriendsFragment.REFRESH_FRIEND_ACTION));
			break;

		case NotifyType.REFUSE_ADD_FRIEND:
			msg = notifyVo.getUser().getName()
					+ mContext.getString(R.string.refuse_add_friend_with_you);
			break;

		case NotifyType.DELETE_FRIEND:
			msg = notifyVo.getUser().getName()
					+ mContext.getString(R.string.unbind_friendship);
			mContext.sendBroadcast(new Intent(
					FriendsFragment.REFRESH_FRIEND_ACTION));
			break;

		case TCNotifyType.APPLY_ADD_GROUP:
			msg = mContext.getString(R.string.apply_add_group);
			break;

		case TCNotifyType.AGREE_ADD_GROUP:
			msg = mContext.getString(R.string.agree_apply_add_group);
			Intent agreeApplyIntent = new Intent(
					GroupDetailActivity.ACTION_AGREE_ADD_GROUP);
			agreeApplyIntent.putExtra("id", notifyVo.getRoomID());
			mContext.sendBroadcast(agreeApplyIntent);
			mContext.sendBroadcast(new Intent(
					GroupChatActivity.REFRESH_CONTACT_GROUP_ACTION));
			break;

		case TCNotifyType.DISAGREE_ADD_GROUP:
			msg = mContext.getString(R.string.disagree_apply_add_group);
			break;

		case TCNotifyType.INVITE_ADD_GROUP:
			msg = mContext.getString(R.string.invite_add_group);
			break;

		case TCNotifyType.AGREE_INVITE_ADD_GROUP:
			msg = mContext.getString(R.string.agree_invite_add_group);
			Intent agreeInviteIntent = new Intent(
					GroupDetailActivity.ACTION_AGREE_ADD_GROUP);
			agreeInviteIntent.putExtra("id", notifyVo.getRoomID());
			mContext.sendBroadcast(agreeInviteIntent);
			mContext.sendBroadcast(new Intent(
					GroupChatActivity.REFRESH_CONTACT_GROUP_ACTION));
			break;

		case TCNotifyType.DISAGREE_INVITE_ADD_GROUP:
			msg = mContext.getString(R.string.disagree_invite_add_group);
			break;

		case TCNotifyType.GROUP_KICK_OUT: // 收到被踢出群的通知，清空数据表中和群相关的会话列表和消息列表，
											// 并发通知更新会话列表页面和未读数

			mContext.sendBroadcast(new Intent(ChatActivity.UPDATE_COUNT_ACTION));
			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));
			msg = mContext.getString(R.string.you_have_been_kick_out_group);

			// 刷新群列表数据
			Intent kickIntent = new Intent(GroupChatActivity.ACTION_KICK_GROUP);
			kickIntent.putExtra("id", notifyVo.getRoomID());
			mContext.sendBroadcast(kickIntent);
			break;

		case TCNotifyType.GROUP_KICK_OUT_OTHER: // 收到群里其他成员被踢出之后的通知
			Intent kickOtherIntent = new Intent(
					GroupDetailActivity.ACTION_REFRESH_GROUP_DETAIL);
			kickOtherIntent.putExtra("groupid", notifyVo.getRoomID());
			mContext.sendBroadcast(kickOtherIntent);
			break;

		case TCNotifyType.EXIT_GROUP:
			Intent exitIntent = new Intent(
					GroupDetailActivity.ACTION_REFRESH_GROUP_DETAIL);
			exitIntent.putExtra("groupid", notifyVo.getRoomID());
			mContext.sendBroadcast(exitIntent);

			break;

		case TCNotifyType.DELETE_GROUP: // 收到群主删除群通知，清空数据表中和群相关的会话列表和消息列表，
										// 并发通知更新会话列表页面和未读数

			mContext.sendBroadcast(new Intent(ChatActivity.UPDATE_COUNT_ACTION));
			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));

			msg = mContext.getString(R.string.group_has_been_deleted_by_admin);

			// 刷新群列表数据
			Intent deleteIntent = new Intent(
					GroupChatActivity.ACTION_DELETE_GROUP);
			deleteIntent.putExtra("id", notifyVo.getRoomID());
			mContext.sendBroadcast(deleteIntent);
			break;

		case TCNotifyType.EXIT_TEMP_CHAT:
			Intent exitChatIntent = new Intent(
					ChatDetailActivity.ACTION_REFRESH_CHAT_DETAIL_PAGE);
			exitChatIntent.putExtra("groupid", notifyVo.getRoomID());
			mContext.sendBroadcast(exitChatIntent);
			break;

		case TCNotifyType.TEMP_CHAT_KICK_OUT:

			mContext.sendBroadcast(new Intent(ChatActivity.UPDATE_COUNT_ACTION));
			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));

			Intent kickTempIntent = new Intent(ACTION_KICK_OR_DELETE_TEMP_CHAT);
			kickTempIntent.putExtra("id", notifyVo.getRoomID());
			kickTempIntent.putExtra("excuteType", 1);
			mContext.sendBroadcast(kickTempIntent);
			break;

		case TCNotifyType.KICK_OUT_TEMP_CHAT:
			Intent kickChatOtherIntent = new Intent(
					ChatDetailActivity.ACTION_REFRESH_CHAT_DETAIL_PAGE);
			kickChatOtherIntent.putExtra("groupid", notifyVo.getRoomID());
			mContext.sendBroadcast(kickChatOtherIntent);
			break;

		case TCNotifyType.DELETE_TEMP_CHAT:

			mContext.sendBroadcast(new Intent(ChatActivity.UPDATE_COUNT_ACTION));
			mContext.sendBroadcast(new Intent(
					HaoXinProjectActivity.UPDATE_SESSION_UNREAD_COUNT));

			Intent deleteTempIntent = new Intent(
					ACTION_KICK_OR_DELETE_TEMP_CHAT);
			deleteTempIntent.putExtra("id", notifyVo.getRoomID());
			deleteTempIntent.putExtra("excuteType", 0);
			mContext.sendBroadcast(deleteTempIntent);
			break;

		case TCNotifyType.MODIFY_TEMP_CHAT_NAME:
			Intent intent = new Intent(
					ChatMainActivity.ACTION_UPDATE_TEMP_CHAT_NAME);
			intent.putExtra("id", notifyVo.getRoomID());
			intent.putExtra("chatType", ChatType.TEMP_CHAT);
			intent.putExtra("name", notifyVo.getRoom().getGroupName());
			mContext.sendBroadcast(intent);
			break;

		default:
			break;
		}

		mContext.sendBroadcast(new Intent(UPDATE_SESSION_UNREAD_COUNT));

		mContext.sendBroadcast(new Intent(
				NotifyActivity.ACTION_NOTIFY_SYSTEM_MESSAGE));
		try {
			ActivityManager am = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			if (cn.getClassName().equals(
					cn.getPackageName() + ".NotifyActivity")) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Notification
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher; // 设置通知的图标
		long currentTime = System.currentTimeMillis();
		if (currentTime - Common.getNotificationTime(mContext) > Common.NOTIFICATION_INTERVAL) {
			if (Common.getOpenSound(mContext)) {
				notification.defaults |= Notification.DEFAULT_SOUND;
			}
			Common.saveNotificationTime(mContext, currentTime);
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// 音频将被重复直到通知取消或通知窗口打开。
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.when = currentTime;

		Intent intent = new Intent(mContext, NotifyActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext,
				NOTION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(mContext,
				mContext.getString(R.string.has_new_notification), msg,
				contentIntent);
		NotificationManager manager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTION_ID, notification);
	}

	/**
	 * 注册通知
	 */
	private void registerNetWorkMonitor() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NETWORK_CHANGE);
		filter.addAction(ACTION_LOGIN_OUT);
		filter.addAction(LOGIN_SUCCESS_ACTION);
		filter.addAction(SYSTEM_EXIT);
		filter.addAction(ACTION_SHOW_TOAST);
		filter.addAction(UPDATE_SESSION_UNREAD_COUNT);
		filter.addAction(ACTION_SEND_FILE_MESSAGE);
		filter.addAction(ACTION_DOWNLOAD_FILE_MESSAGE);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
	}

	/**
	 * 处理通知类
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_NETWORK_CHANGE)) {
				boolean isNetConnect = false;
				ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				NetworkInfo activeNetInfo = connectivityManager
						.getActiveNetworkInfo();
				if (activeNetInfo != null) {
					if (activeNetInfo.isConnected()) {
						isNetConnect = true;
					} else {
						Toast.makeText(
								context,
								mContext.getResources().getString(
										R.string.network_error)
										+ " " + activeNetInfo.getTypeName(),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(
							context,
							mContext.getResources().getString(
									R.string.network_error), Toast.LENGTH_SHORT)
							.show();
				}
				Common.setNetWorkState(isNetConnect);
			} else if (ACTION_LOGIN_OUT.equals(action)) {
				Intent m_intent = new Intent(HaoXinProjectActivity.this,
						XMPPChatListenerService.class);
				stopService(m_intent);
				// Intent m_intent1 = new Intent(HaoXinProjectActivity.this,
				// XMPPFileListenerService.class);
				// stopService(m_intent1);
				XmppTool.con = null;
				Intent loginIntent = new Intent(mContext, TeLoginActivity.class);
				loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(loginIntent);
				finish();

			} else if (LOGIN_SUCCESS_ACTION.equals(action)) {
				/**
				 * new
				 * **/
				Intent m_intent = new Intent(HaoXinProjectActivity.this,
						XMPPChatListenerService.class);
				HaoXinProjectActivity.this.startService(m_intent);

				// Intent m_intent1 = new Intent(HaoXinProjectActivity.this,
				// XMPPFileListenerService.class);
				// HaoXinProjectActivity.this.startService(m_intent1);
				onCheckedChanged(bottomRadioGroup, R.id.radio_haoxin);
				/**
				 * new
				 * **/

				// 登录XMPP

				// String host = Constants.OF_HOST;
				// String port = Constants.OF_PORT;
				// String loginStr = TCChatManager.getInstance().loginXmpp(host,
				// port, Constants.OF_RESOURSE_NAME, Constants.uid, "888888",
				// mLoginListenser);
				// // 根据返回值不为空判断是否存在传入的参数有误
				// if (!TextUtils.isEmpty(loginStr)) {
				// }
				// // 初始化会话页面
				// // onSwitchContentFrom(ChatFragment.class.getName(), null);
				// if ("2".equals(getIntent().getStringExtra("type"))) {
				// onCheckedChanged(bottomRadioGroup, R.id.radio_contact);
				// } else {
				// onCheckedChanged(bottomRadioGroup, R.id.radio_haoxin);
				// }
				// 显示总的未读数
				// updateSessionCount();
			} else if (SYSTEM_EXIT.equals(action)) {
				HaoXinProjectActivity.this.finish();
				System.exit(0);
			} else if (ACTION_SHOW_TOAST.equals(action)) {

				new AlertDialog.Builder(mContext)
						.setTitle("提示")
						.setMessage("账号已在别处登录，确定退出")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										sf = mContext.getSharedPreferences(
												"basic_info",
												Context.MODE_PRIVATE);
										DatabaseHelper dbOpenHelper = DatabaseHelper
												.getInstance(mContext);
										dbOpenHelper.closeDB();
										Editor editor1 = sf.edit();
										editor1.putBoolean("islogin", false);
										editor1.putBoolean(
												"isNeedVoluntaryLogin", false);
										editor1.putString("username", null);
										editor1.putString("password", null);
										editor1.putString("userID", "");
										editor1.commit();

										SharedPreferences preferences = mContext
												.getSharedPreferences(
														Common.LOGIN_SHARED, 0);
										Editor editor = preferences.edit();
										editor.remove(Common.LOGIN_RESULT);
										editor.commit();
										Common.setUid("");
										Common.setToken("");
										mContext.sendBroadcast(new Intent(
												HaoXinProjectActivity.ACTION_LOGIN_OUT));
										setlogout();

									}
								}).show();

			} else if (UPDATE_SESSION_UNREAD_COUNT.equals(action)) {
				updateSessionCount();
			} else if (ACTION_SEND_FILE_MESSAGE.equals(action)) {
				TCMessage message = (TCMessage) intent
						.getSerializableExtra("message");

				sendMessage(message);
			} else if (ACTION_DOWNLOAD_FILE_MESSAGE.equals(action)) {
				TCMessage message = (TCMessage) intent
						.getSerializableExtra("message");

				// downloadFile(message);
			}
		}
	};

	/**
	 * 注销账号
	 */

	private void setlogout() {

		HashMap<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("userId", Constants.userId);
		requestMap.put("type", "loginOut");

		try {
			ServerAdaptor.getInstance(HaoXinProjectActivity.this).doAction(1,
					"LoginAction$loginOut", requestMap,
					new ServiceSyncListener() {

						@Override
						public void onError(ActionResponse returnObject) {
							super.onError(returnObject);

						}

						@Override
						public void onSuccess(ActionResponse returnObject) {
							Log.e(TAG, "登出成功");

						}
					});
		} catch (HaoqeeException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsRegisterReceiver) {
			unregisterReceiver(mReceiver);
		}
		unregisterReceiver(refreshRemind);
		unregisterReceiver(zg_broad);
		unregisterReceiver(openfire_msg);
	}

	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
	}

	private void updateSessionCount() {

		if (sp.getBoolean("isread_sr", false)) {
			remindTv_sr.setVisibility(View.VISIBLE);
		} else {
			remindTv_sr.setVisibility(View.GONE);
		}

		if (sp.getBoolean("isread_my", false)) {
			remindTv_my.setVisibility(View.VISIBLE);
		} else {
			remindTv_my.setVisibility(View.GONE);
		}

	}

	private void sendMessage(final TCMessage msg) {
		TCChatManager.getInstance().sendMessage(msg, new TCMessageListener() {

			@Override
			public void onError(TCError error) {

			}

			@Override
			public void doComplete() {

			}

			@Override
			public void onError(TCMessage tcMessage, TCError error) {
				// 消息发送出错进入该回调
				Intent intent = new Intent(
						ChatMainActivity.ACTION_SEND_FILE_FAILED);
				intent.putExtra("message", tcMessage);
				intent.putExtra("error", error);
				mContext.sendBroadcast(intent);
			}

			@Override
			public void doComplete(TCMessage tcMessage) {
				Intent intent = new Intent(
						ChatMainActivity.ACTION_SEND_FILE_COMPLETE);
				intent.putExtra("message", tcMessage);
				mContext.sendBroadcast(intent);
			}

			@Override
			public void onProgress(int progress) {
				msg.getFileMessageBody().setUploadProgress(progress);
				Intent intent = new Intent(
						ChatMainActivity.ACTION_UPDATE_FILE_PROGRESS);
				intent.putExtra("message", msg);
				mContext.sendBroadcast(intent);
			}
		});
	}

	public String getVersion() {
		String appVersion = "";
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			appVersion = info.versionName; // 版本名
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appVersion;
	}

}

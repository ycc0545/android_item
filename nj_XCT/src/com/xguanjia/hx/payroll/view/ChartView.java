package com.xguanjia.hx.payroll.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

public class ChartView extends View {
	public int XPoint = 90; // 原点的X坐标
	public int YPoint = 360; // 原点的Y坐标
	public int XScale = 50; // X的刻度长度
	public int YScale = 60; // Y的刻度长度
	public int XLength = 600; // X轴的长度
	public int YLength = 360; // Y轴的长度
	public String[] XLabel; // X的刻度
	public String[] YLabel; // Y的刻度
	public String[] Data; // 数据
	public String Title; // 显示的标题

	public float fdensity;

	public ChartView(Context context) {
		super(context);
	}

	public void SetInfo(String[] XLabels, String[] YLabels, String[] AllData, String strTitle, int width, int height) {
		XPoint = 90;
		YPoint = (int) ((int) height / 3.4);
		XLength = width - 120;
		YLength = (int) ((int) height / 3.4);
		XScale = XLength / 12;
		YScale = (YLength / 6) + 1;

		XLabel = XLabels;
		YLabel = YLabels;
		Data = AllData;
		Title = strTitle;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);// 重写onDraw方法

		// canvas.drawColor(Color.WHITE);//设置背景颜色
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);// 去锯齿
		paint.setColor(Color.parseColor("#D9D9D9"));// 颜色
		Paint paint1 = new Paint();
		paint1.setStyle(Paint.Style.STROKE);
		paint1.setStyle(Paint.Style.FILL);
		paint1.setAntiAlias(true);// 去锯齿
		paint1.setStrokeWidth(4);
		paint1.setColor(Color.parseColor("#0085D0"));
		paint1.setTextSize(35);
		paint.setTextSize(20); // 设置轴文字大小
		//Y轴的值的标记
		Paint paint3 = new Paint();
		paint3.setStyle(Paint.Style.STROKE);
		paint3.setAntiAlias(true);// 去锯齿
		paint3.setColor(Color.parseColor("#D9D9D9"));// 颜色
		paint3.setTextAlign(Align.RIGHT);
		paint3.setTextSize(20);
	
		// 设置Y轴

		canvas.drawLine(XPoint, YPoint - YLength, XPoint, YPoint, paint); // 轴线
		for (int i = 0; i * YScale < YLength; i++) {
			canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + 5, YPoint - i * YScale, paint); // 刻度
			canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + XLength - XScale, YPoint - i * YScale, paint); // 各个刻度的线条
			try {
				
				canvas.drawText(YLabel[i], XPoint - 8, YPoint - i * YScale + 5, paint3); // 文字
				// myStaticLayout.draw(canvas);
			} catch (Exception e) {
			}
		}
	
		canvas.drawLine(XPoint, YPoint - YLength, XPoint - 3, YPoint - YLength + 6, paint); // 箭头
		canvas.drawLine(XPoint, YPoint - YLength, XPoint + 3, YPoint - YLength + 6, paint);
		// 设置X轴
		canvas.drawLine(XPoint, YPoint, XPoint + XLength, YPoint, paint); // 轴线
		for (int i = 0; i * XScale < XLength; i++) {
			canvas.drawLine(XPoint + i * XScale, YPoint, XPoint + i * XScale, YPoint - 5, paint); // 刻度
			canvas.drawLine(XPoint + i * XScale, YPoint, XPoint + i * XScale, YPoint - YLength + YScale, paint); // 各个刻度的线条
			try {
				canvas.drawText(XLabel[i], XPoint + i * XScale - 7, YPoint + 24, paint); // 文字
				// 数据值
				if (i > 0 && YCoord(Data[i - 1]) != -999 && YCoord(Data[i]) != -999) // 保证有效数据
					canvas.drawLine(XPoint + (i - 1) * XScale, YCoord(Data[i - 1]), XPoint + i * XScale, YCoord(Data[i]), paint1);
				canvas.drawCircle(XPoint + i * XScale, YCoord(Data[i]), 6, paint1);
			} catch (Exception e) {
			}
		}
		canvas.drawLine(XPoint + XLength, YPoint, XPoint + XLength - 6, YPoint - 3, paint); // 箭头
		canvas.drawLine(XPoint + XLength, YPoint, XPoint + XLength - 6, YPoint + 3, paint);
		canvas.drawText(Title, 250, 30, paint1);
	}

	public int shipei_value(int value) {

		if (fdensity == 1.33f) {
			return value;
		} else {
			float scale = fdensity / 1.33f;
			float value_temp = (float) value;
			value_temp = scale * value_temp;
			return (int) value_temp;
		}

	}

	private int YCoord(String y0) // 计算绘制时的Y坐标，无数据时返回-999
	{
		int y;
		try {
			y = Integer.parseInt(y0);
		} catch (Exception e) {
			return -999; // 出错则返回-999
		}
		try {
			return YPoint - y * YScale / Integer.parseInt(YLabel[1]);
		} catch (Exception e) {
		}
		return y;
	}
}
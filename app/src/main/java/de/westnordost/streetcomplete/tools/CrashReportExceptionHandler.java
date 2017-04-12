package de.westnordost.streetcomplete.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class CrashReportExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
	private final Context appCtx;

	private final static String CRASHREPORT = "crashreport.txt";
	private final static String ENC = "UTF-8";
	private final static String MAILTO = "osm@westnordost.de";
	private final static String GOOGLEPLAY = "com.android.vending";

	@Inject public CrashReportExceptionHandler(Context appCtx)
	{
		this.appCtx = appCtx;
		install();
	}

	private boolean install()
	{
		String installerPackageName = appCtx.getPackageManager().getInstallerPackageName(appCtx.getPackageName());
		// developer. Don't need this functionality (it might even interfere with unit tests)
		if(installerPackageName == null) return false;
		// don't need this for google play users: they have their own crash reports
		if(GOOGLEPLAY.equals(installerPackageName)) return false;

		Thread.UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
		if(ueh instanceof CrashReportExceptionHandler)
		{
			throw new IllegalStateException("May not install several CrashReportExceptionHandlers!");
		}

		defaultUncaughtExceptionHandler = ueh;
		Thread.setDefaultUncaughtExceptionHandler(this);
		return true;
	}

	public void askUserToSendCrashReportIfExists(Activity activityCtx)
	{
		if(hasCrashReport())
		{
			String reportText = readCrashReportFromFile();
			deleteCrashReport();
			askUserToSendErrorReport(activityCtx, R.string.crash_title, reportText);
		}
	}

	public void askUserToSendErrorReport(Activity activityCtx, int titleResourceId, Exception e)
	{
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		askUserToSendErrorReport(activityCtx, titleResourceId, stackTrace.toString());
	}

	private void askUserToSendErrorReport(final Activity activityCtx, final int titleResourceId, String error)
	{
		final String report =
				"Describe how to reproduce it here:\n\n\n\n" +
				getDeviceInformationString() + "\n" + error;

		new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run()
		{
			new AlertDialogBuilder(activityCtx)
					.setTitle(titleResourceId)
					.setMessage(R.string.crash_message)
					.setPositiveButton(R.string.crash_compose_email, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							sendEmail(activityCtx, report);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							Toast.makeText(activityCtx, "\uD83D\uDE22",Toast.LENGTH_SHORT).show();
						}
					})
					.setCancelable(false)
					.show();
		}});
	}

	@Override public void uncaughtException(Thread t, Throwable e)
	{
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		writeCrashReportToFile(
				getThreadString(t) +
				"\nStack trace:\n" + stackTrace.toString());
		defaultUncaughtExceptionHandler.uncaughtException(t, e);
	}

	private String getDeviceInformationString()
	{
		return "Device: " + Build.BRAND + " " + Build.DEVICE + ", Android " + Build.VERSION.RELEASE;
	}

	private String getThreadString(Thread t)
	{
		return "Thread: " + t.getName();
	}

	private void writeCrashReportToFile(String text)
	{
		try
		{
			FileOutputStream fos = appCtx.openFileOutput(CRASHREPORT, Context.MODE_PRIVATE);
			fos.write(text.getBytes(ENC));
			fos.close();
		}
		catch (IOException e)	{}
	}

	private boolean hasCrashReport()
	{
		return Arrays.asList(appCtx.fileList()).contains(CRASHREPORT);
	}

	private String readCrashReportFromFile()
	{
		try
		{
			FileInputStream fis = appCtx.openFileInput(CRASHREPORT);
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			fis.close();
			return result.toString(ENC);
		}
		catch (IOException e) {}
		return null;
	}

	private void deleteCrashReport()
	{
		appCtx.deleteFile(CRASHREPORT);
	}

	private void sendEmail(Activity activityCtx, String text)
	{
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("mailto:"));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {MAILTO});
		intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Error Report");
		intent.putExtra(Intent.EXTRA_TEXT, text);
		if (intent.resolveActivity(activityCtx.getPackageManager()) != null) {
			activityCtx.startActivity(intent);
		}
		else
		{
			Toast.makeText(activityCtx, R.string.no_email_client, Toast.LENGTH_SHORT).show();
		}
	}
}

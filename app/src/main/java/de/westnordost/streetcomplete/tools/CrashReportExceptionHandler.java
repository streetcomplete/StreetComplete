package de.westnordost.streetcomplete.tools;

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

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class CrashReportExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
	private final Context ctx;

	private final static String FILENAME = "crashreport.txt";
	private final static String ENC = "UTF-8";
	private final static String MAILTO = "osm@westnordost.de";
	private final static String GOOGLEPLAY = "com.android.vending";

	public static void install(Context ctx)
	{
		String installerPackageName = ctx.getPackageManager().getInstallerPackageName(ctx.getPackageName());
		if(installerPackageName == null) // developer. Don't need this functionality
			return;
		// don't need this for google play users: it has their own crash reports
		if(GOOGLEPLAY.equals(installerPackageName))
			return;

		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashReportExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(
					new CrashReportExceptionHandler(ctx)
			);
		}
	}

	private CrashReportExceptionHandler(Context ctx)
	{
		this.ctx = ctx;
		defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

		checkAndSendReport();
	}

	private void checkAndSendReport()
	{
		if(hasReport())
		{
			final String reportText = readReportFromFile();
			deleteReport();

			new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run()
			{
				new AlertDialogBuilder(ctx)
						.setTitle(R.string.crash_title)
						.setMessage(R.string.crash_message)
						.setPositiveButton(R.string.crash_compose_email, new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								sendEmail(reportText);
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								Toast.makeText(ctx, "\uD83D\uDE22",Toast.LENGTH_SHORT).show();
							}
						})
						.show();
			}});
		}
	}

	@Override public void uncaughtException(Thread t, Throwable e)
	{
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		writeReportToFile(
				"Device: " + Build.BRAND + " " + Build.DEVICE +	", Android " + Build.VERSION.RELEASE +
				"\n\nStack trace:\n" + stackTrace.toString()
		);

		defaultUncaughtExceptionHandler.uncaughtException(t, e);
	}

	private void writeReportToFile(String text)
	{
		try
		{
			FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(text.getBytes(ENC));
			fos.close();
		}
		catch (IOException e)	{}
	}

	private boolean hasReport()
	{
		return Arrays.asList(ctx.fileList()).contains(FILENAME);
	}

	private String readReportFromFile()
	{
		try
		{
			FileInputStream fis = ctx.openFileInput(FILENAME);
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

	private void deleteReport()
	{
		ctx.deleteFile(FILENAME);
	}

	private void sendEmail(String text)
	{
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("mailto:"));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {MAILTO});
		intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Error Report");
		intent.putExtra(Intent.EXTRA_TEXT, "Describe how to reproduce it here:\n\n\n\n\n" + text);
		if (intent.resolveActivity(ctx.getPackageManager()) != null) {
			ctx.startActivity(intent);
		}
		else
		{
			Toast.makeText(ctx, R.string.no_email_client, Toast.LENGTH_SHORT).show();
		}
	}
}

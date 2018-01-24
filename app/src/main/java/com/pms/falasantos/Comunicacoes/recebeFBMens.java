package com.pms.falasantos.Comunicacoes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pms.falasantos.Atividades.MainActivity;
import com.pms.falasantos.Globais;
import com.pms.falasantos.R;

import java.util.Map;
/**
 * Created by w0513263 on 26/07/17.
 */

public class recebeFBMens extends FirebaseMessagingService
	{
	/**
	 * onMessageReceived  recebe mensagens do FB quando o programa estiver ativo e no ar
	 * @param fbmens
	 */
	@Override
	public void onMessageReceived( RemoteMessage fbmens )
		{
		Log.i( Globais.apptag, "From: " + fbmens.getFrom() );
		
		RemoteMessage.Notification notif = fbmens.getNotification();
		String title = notif.getTitle();
		String body = notif.getBody();
		Map data = fbmens.getData();

		String url = "";
		if( !data.containsKey( "tipo" ) )
			return;
		String aux = data.get( "tipo").toString();
		int tipo = Integer.parseInt( aux );
		switch( tipo )
			{
			case 1:
				
				break;
			case 2:
				url = data.get( "url" ).toString();
				Uri uri = Uri.parse( url );
				Intent intent = new Intent(Intent.ACTION_VIEW, uri );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(intent);
				break;
			}
		
		//
		if( fbmens.getData().size() > 0 )
			{
			Log.i( Globais.apptag, "Message data payload: " + fbmens.getData() );
			
			if( true )
				{
				// For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
				scheduleJob();
				}
			else
				{
				// Handle message within 10 seconds
				handleNow();
				}
				
			}

		// verifica se a mensagem tem uma notificação
		if( fbmens.getNotification() != null )
			{
			Log.i( Globais.apptag, "Message Notification Body: " + fbmens.getNotification().getBody() );
			Log.i( Globais.apptag, "Message Notification Title: " + fbmens.getNotification().getTitle() );
			}
		
		sendNotification( "Texto do sendNotification" );
		}
// [END receive_message]
	
	/**
	 * Schedule a job using FirebaseJobDispatcher.
	 */
	private void scheduleJob()
		{
		Log.i( Globais.apptag, "scheduleJob");
		}
	
	/**
	 * Handle time allotted to BroadcastReceivers.
	 */
	private void handleNow()
		{
		Log.i( Globais.apptag, "Short lived task is done." );
		}
	
	/**
	 * Notificação
	 * @param messageBody FCM message body received.
	 */
	private void sendNotification( String messageBody )
		{
		Intent intent = new Intent( this, MainActivity.class );
		intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
		PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 , intent,
		                                                         PendingIntent.FLAG_ONE_SHOT );
		
		Uri defaultSoundUri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( this )
			.setSmallIcon( R.mipmap.ic_launcher_round )
			.setContentTitle( "FCM Message" )
			.setContentText( messageBody )
			.setAutoCancel( true )
			.setSound( defaultSoundUri )
			.setContentIntent( pendingIntent );
		
		NotificationManager notificationManager =
			(NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		
		notificationManager.notify( 0 , notificationBuilder.build() );
		}
	}
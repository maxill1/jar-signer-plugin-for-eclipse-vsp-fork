/*
 * Creado el 17/12/2004
 *
 * Para cambiar la plantilla para este archivo generado vaya a
 * Ventana&gt;Preferencias&gt;Java&gt;Generación de código&gt;Código y comentarios
 */
package it.takethesecoins.jarsigner.jarsigner;



import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author PACO
 *
 * Para cambiar la plantilla para este comentario de tipo generado vaya a
 * Ventana&gt;Preferencias&gt;Java&gt;Generación de código&gt;Código y comentarios
 */
public class StreamCoupler extends Thread {
	InputStream in;
	OutputStream out;
	StringBuffer log;
	public StreamCoupler(InputStream unIn, OutputStream unOut){
		in=unIn;
		out=unOut;
	}

	public void run(){
		if (in==null||out==null)return;
		byte []buffer = new byte[2000];
		try{

			log = new StringBuffer();
			while(true){
				//MensajesDebug.imprimeMensaje("BUFFER="+in.available());
				int lectura=0;
				//			if(in.available()==0)
				//				lectura = in.read(buffer,0,1);
				//			else
				lectura = in.read(buffer);

				if (lectura<0) break;
				if (lectura>0){
					out.write(buffer,0,lectura);
					out.flush();
					for (int i = 0; i < buffer.length; i++) {
						log.append((char)buffer[i]);						
					}
				}

			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getLog() {
		return log == null ? "" : log.toString();
	}
}
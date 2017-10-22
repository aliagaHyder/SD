package httpServer;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerHilo extends Thread{

	private Socket socketCliente;
	private Socket socketController;
	private int puertoController;
	private String hostController;
	private String comando;
	
	// INICIALIZA LOS ATRIBUTOS DEL HILO E
	// INICIA A PROCESAR LA TRAMA HTTP DESDE EL MÉTODO RUN()
	public ServerHilo ( Socket _sc, int _pc, String _hc )
	{	
		socketCliente = _sc;
		puertoController = _pc;
		hostController = _hc;
		this.procesarPeticion();
	}
	
	// PARA LEER EL SOCKET, USADO PARA LEER LA TRAMA
	// HTTP ENVIADA POR EL NAVEGADOR
	public String leerSocket ( Socket _sc, String _buffer )
	{
		try
		{
			InputStream aux = _sc.getInputStream();
			
			BufferedReader entrada = new BufferedReader(new InputStreamReader(aux));;
			
			_buffer = new String();
			
			while ( entrada.ready() )
			{
				_buffer = _buffer.concat(entrada.readLine() + "\n"); 
			}
			
			return _buffer;
			
		}
		catch ( Exception e )
		{
			System.out.print( "ERROR: al leer socket cliente" );
			System.out.println( e.toString() );
		}
		
		return "";
	}
	

	// GUARDA EN UN BUFFER EL ARCHIVO EL CONTENIDO
	// ESTÁTICO CORRESPONDIENTE Y SE PREPARA PARA ESCRIBIRLO
	// EN UNA PETICIÓN HTTP, SIENDO ESCRITA EN EL SOCKET
	public void enviarEstatico( String [] cadena )
	{
		System.out.println("");
		System.out.println("[Procesando petición estatica...]");
		try 
		{
			PrintWriter salida;
			salida = new PrintWriter( socketCliente.getOutputStream() );
			
			BufferedReader br;
			
			String datos = "";
			String archivo = "./";
			
			// ENVIA LA PÁGINA PRINCIPAL DEL INVERNADERO
			if ( cadena.length == 0 || cadena[1].equals("index.html") )
			{

				System.out.println("[preparando index.html]");
				archivo = archivo.concat("index.html");
				
				br = new BufferedReader( new FileReader( archivo ) );
				
				// CABECERA DE LA RESPUESTA HTTP
				salida.println("HTTP/1.1 200 OK");
				salida.println("Content-Type: text/html; charset=utf-8");
				salida.println("Server: MyHTTPServer");
				
				salida.println("");
				
				// AÑADIMOS AL COTENIDO ESTÁTICO LA CABECERA HTTP
				datos = br.readLine();
				while ( datos != null )
				{
					salida.println( datos );
					datos = br.readLine();
				}
				
				salida.flush();
				System.out.println("[index enviado]");
			}
			// PÁGINA DE ERROR EN CASO DE URL INCORRECTA
			else
			{
				System.out.println("[erro en la url]");
				System.out.println("[preparando error.html]");
				archivo = archivo.concat("error.html");
				
				br = new BufferedReader( new FileReader( archivo ));
				
				// CABECERA DE LA RESPUESTA HTTP
				salida.println("HTTP/1.1 404 Not Found");
				salida.println("Content-Type: text/html; charset=utf-8");
				salida.println("Server: MyHTTPServer");
				
				salida.println("");
				
				// AÑADIMOS AL COTENIDO ESTÁTICO LA CABECERA HTTP
				datos = br.readLine();
				while ( datos != null )
				{
					salida.println( datos );
					datos = br.readLine();
				}
				System.out.println("[pagina error enviada]");
				salida.flush();
			}
			
		} 
		catch (IOException e) 
		{
			System.out.println("ERROR: al enviar estático");
			e.printStackTrace();
		}
			
	}
	
    // ESCRIBE EN EL SOCKET ENTRE SERVIDOR-CLIENTE
	// LOS DATOS PASADOS POR PARÁMETRO
   public void escribeSocket(Socket sk, String datos) 
   {
       try 
       {
           OutputStream aux = sk.getOutputStream();
           DataOutputStream flujo = new DataOutputStream(aux);
           flujo.writeUTF(datos);
       } 
       catch (Exception e) 
       {
           System.out.println("No se ha podido conectar con el controlador: " + e.toString());
       }
   }
	
   // CUANDO EN LA URL SE PIDA VALORES DE LAS SONDAS
   // DESDE ESTA FUNCIÓN LLAMAREMOS AL CONTROLADOR
   // PARA QUE NOS DEVUELVA LA PÁGINA EN FUNCIÓN DE LOS
   // DATOS PEDIDOS POR LA URL
   public void enviarDinamico( String [] cadena )
   {
		System.out.println("");
		System.out.println("[Procesando petición dińamica...]");
		try
		{
			// conectar con el socket del controller
			this.socketController = new Socket(this.hostController, this.puertoController);
			
			this.escribeSocket(socketController, this.comando);
			
			System.out.println("[Peticion enviada al controller]");
			this.socketController.close();
			
		}
		catch ( Exception e)
		{
			System.out.println("ERROR: conectando con el controller");
			System.out.println(e.toString());
		}
		
		System.out.println("Conectado");
		
   }
	
	// PROCESA LA TRAMA HTTP ENVIADA POR EL NAVEGADOR
	// COMPRUEBA DE LA PRIMERA LINEA EL TIPO DE PETICION
	// Y LA URL
	public void procesarPeticion ()
	{
		String aux1 = "";
		String buffer = "";
		
		buffer = this.leerSocket(socketCliente, buffer);
		
		if ( !buffer.isEmpty() )
		{
			StringTokenizer s = new StringTokenizer( buffer );

			// guardamos tipo de peticion
			aux1 = s.nextToken().toString();
			
			if ( aux1.equals("GET") )
			{
				aux1 = s.nextToken().toString();
				this.comando = aux1;
				
				String [] cadena = aux1.split("/");

				if ( cadena.length == 0 || !cadena[1].equals("controladorSD") )
				{
					System.out.print("@petición estatica detectada");
					enviarEstatico( cadena );
				}
				else
				{
					System.out.println("@petición dinamica");
					enviarDinamico( cadena );
				}
			}
		}
	}
}




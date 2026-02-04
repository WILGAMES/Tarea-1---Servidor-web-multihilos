package TAREA;

import java.io.* ;
import java.net.* ;
import java.util.* ;


public final class ServidorWeb{
public static void main(String argv[]) throws Exception
        {
                //Establecer el puerto
                int puerto = 6789;
                //Estavlecer el socket que escuchará las conexiones entrantes
                ServerSocket servidor = new ServerSocket(puerto);
                
                System.out.println("Servidor Web ejecutándose en puerto " + puerto);
                
                //Procesar las solicitudes entrantes
                while (true)
                {
                        //Escuchar la solicitud de un cliente
                        Socket socket = servidor.accept();
                        //Objeto que procesa la solicitud del cliente
                        SolicitudHttp solicitud = new SolicitudHttp(socket);
                        //Crear un hilo para procesar la solicitud
                        Thread hilo = new Thread(solicitud);
                        //Iniciar el hilo
                        hilo.start();
        }
}
}

final class SolicitudHttp implements Runnable {
        final static String CRLF = "\r\n";
        Socket socket;

        public SolicitudHttp(Socket socket) throws Exception {
                this.socket = socket;
        }

        public void run() {
                try {
                        procesarSolicitud();
                } catch (Exception e) {
                        System.out.println(e);
                }
        }
        private void procesarSolicitud() throws Exception {
                 //Referencia al stream de entrada del socket
                 InputStream entrada = socket.getInputStream();
               //Referencia al stream de salida del socket
               DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
               //Referencia al stream del buffer del socket
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Leer la línea de solicitud
                String lineaSolicitud = br.readLine();
                
                //Mostrar la línea de solicitud en la consola
                System.out.println(lineaSolicitud);

                //Extraer el nombre del recurso solicitado
                StringTokenizer tokens = new StringTokenizer(lineaSolicitud);
                tokens.nextToken(); //Ignorar el método HTTP(GET)

                //Extraer el nombre del recurso
                String nombreRecurso = tokens.nextToken();
                
                //Anexamos el punto para indicar que es un archivo local
                nombreRecurso = "." + nombreRecurso;

                FileInputStream archivoRecurso = null;
                boolean archivoExiste = true;

                try {
                        //Intentar abrir el archivo solicitado
                        archivoRecurso = new FileInputStream(nombreRecurso);
                } catch (FileNotFoundException e) {
                        //El archivo no existe
                        archivoExiste = false;
                }

                String lineaEstado = null;
                String lineaTipoContenido = null;
                String cuerpoMensaje = null;

                if(archivoExiste){
                        lineaEstado = "HTTP/1.1 200 OK" + CRLF;
                        lineaTipoContenido = "Content-Type: " + contentType(nombreRecurso) + CRLF;
                }else{
                        lineaEstado = "HTTP/1.1 404 Not Found" + CRLF;
                        lineaTipoContenido = "Content-Type: text/html" + CRLF;
                        cuerpoMensaje = "<HTML>" + 
                "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                "<BODY><b>404</b> Not Found</BODY></HTML>";
                }

                //Enviar la línea de estado
                salida.writeBytes(lineaEstado);
                //Enviar la línea de tipo de contenido
                salida.writeBytes(lineaTipoContenido);
                //Enviar una línea en blanco para indicar el fin de las cabeceras
                salida.writeBytes(CRLF);
                //Enviar el cuerpo del mensaje
                if(archivoExiste){
                        //Enviar el archivo solicitado
                        enviarBytes(archivoRecurso, salida);
                        archivoRecurso.close();
                }else{
                        //Enviar el mensaje de error 404
                        salida.writeBytes(cuerpoMensaje);
                }




                //Cerrar los streams y el socket
                salida.close();
                entrada.close();
                socket.close();
        }

        private static void enviarBytes(FileInputStream archivoRecurso, DataOutputStream salida) throws Exception {
                //Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket
                byte[] buffer = new byte[1024];
                int bytes = 0;
                //Copia el archivo solicitado hacia el output stream del socket
                while((bytes = archivoRecurso.read(buffer)) != -1){
                        salida.write(buffer, 0, bytes);
                }
        }

        private static String contentType(String nombreRecurso){
                if(nombreRecurso.endsWith(".htm") || nombreRecurso.endsWith(".html")){
                        return "text/html";
                }
                if(nombreRecurso.endsWith(".jpg") || nombreRecurso.endsWith(".jpeg")){
                        return "image/jpeg";
                }
                if(nombreRecurso.endsWith(".gif")){
                        return "image/gif";
                }
                return "application/octet-stream";
        }
          
 
}


package app;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Biblioteca {

    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final String CATALOG_LIBRO = "catalogo.txt";
    private static final String USUARIOS_LOG = "usuarios.txt";
    private static final String PRESTAMOS_LIBRO = "prestamos.txt";
    private static final String RESERVA_LIBRO = "reserva.txt";
    

    private List<Libro> libros;
    private List<Usuario> usuarios;
    private List<Prestamo> prestamos;
    private final ReentrantLock lock;
    private Fichero fichero;
    private Map<String, String> estadosLibros;

    public Biblioteca() {
        lock = new ReentrantLock();
        fichero = new Fichero();
        
        estadosLibros = new HashMap<>();

        crearArchivos();

        libros = new ArrayList<>();
        usuarios = new ArrayList<>();
        prestamos = new ArrayList<>();

        cargarDatos();
        
        for (Libro libro : libros) {
        estadosLibros.put(libro.getID(), "DISPONIBLE");
    }
    }
    

    private void crearArchivos() {
        crearArchivoSiNoExiste(CATALOG_LIBRO);
        crearArchivoSiNoExiste(USUARIOS_LOG);
        crearArchivoSiNoExiste(PRESTAMOS_LIBRO);
        crearArchivoSiNoExiste(RESERVA_LIBRO);
    }

    private void crearArchivoSiNoExiste(String nombreArchivo) {
        File file = new File(nombreArchivo);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cargarDatos() {
        cargarLibros();
        cargarUsuarios();
    }

    public void agregarLibro(Libro libro) {
        if (libro == null || libro.getTitulo() == null || libro.getAutor() == null || libro.getEstado() == null) {
            JOptionPane.showMessageDialog(null, "Fallo en el registro, los datos no pueden ser nulos");
            return;
        }

        lock.lock();
        try {
            if (obtenerLibroPorTitulo(libro.getTitulo()) != null) {
                JOptionPane.showMessageDialog(null, "El libro ya existe.");
                return;
            }

            String nuevoID = String.valueOf(obtenerProximoID(CATALOG_LIBRO));
            libro.setID(nuevoID);

            List<String> lineas = fichero.leerArchivo(CATALOG_LIBRO);
            lineas.add(libro.toString());
            fichero.escribirArchivo(CATALOG_LIBRO, lineas);
            libros.add(libro);
            JOptionPane.showMessageDialog(null, "Libro registrado exitosamente.");
        } finally {
            lock.unlock();
        }
    }

    public void agregarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getNombre() == null || usuario.getTipoUsuario() == null) {
            JOptionPane.showMessageDialog(null, "Fallo en el registro, los datos no pueden ser nulos");
            return;
        }

        lock.lock();
        try {
            if (obtenerUsuarioPorNombre(usuario.getNombre()) != null) {
                JOptionPane.showMessageDialog(null, "El usuario ya existe.");
                return;
            }

            String nuevoID = String.valueOf(obtenerProximoID(USUARIOS_LOG));
            usuario.setID(nuevoID);

            List<String> lineas = fichero.leerArchivo(USUARIOS_LOG);
            lineas.add(usuario.toString());
            fichero.escribirArchivo(USUARIOS_LOG, lineas);
            usuarios.add(usuario);
            JOptionPane.showMessageDialog(null, "El usuario ha sido registrado exitosamente");
        } finally {
            lock.unlock();
        }
    }

    public void realizarPrestamo() {
        executorService.submit(() -> {
            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del lector:");
            String titulo = JOptionPane.showInputDialog("Ingrese el titulo del libro a prestar:");

            List<String> lineasU = fichero.leerArchivo("usuarios.txt");
            List<String> lineasL = fichero.leerArchivo("catalogo.txt");
            boolean usuarioEncontrado = false;
            boolean libroEncontrado = false;
            Usuario u = null;
            Libro libroSolicitado = null;

            // Encontrar usuario
            for (String linea : lineasU) {
                Usuario usuario = convertirLineaUsuario(linea);
                if (usuario.getNombre().equalsIgnoreCase(nombre)) {
                    usuarioEncontrado = true;
                    u = usuario;
                    break;
                }
            }
            // Encontrar libro
            for (String linea : lineasL) {
                Libro libro = convertirLineaLibro(linea);
                if (libro.getTitulo().equals(titulo)) {
                    libroEncontrado = true;
                    libroSolicitado = libro;
                    break;
                }
            }

            if (usuarioEncontrado && libroEncontrado) {
                estadosLibros.put(libroSolicitado.getID(),"Prestado");
                LocalDate fechaInicio = LocalDate.now();
                LocalDate fechaDevolucion = fechaInicio.plusWeeks(2);
                String nuevoID = String.valueOf(obtenerProximoID(USUARIOS_LOG));
                Prestamo prestamo = new Prestamo(nuevoID, libroSolicitado, u, fechaInicio, fechaDevolucion);
                prestamos.add(prestamo);
                
                actualizarEstadoLibroEnArchivo(libroSolicitado.getID(),"Prestado");

                JOptionPane.showMessageDialog(null, "Préstamo realizado exitosamente.");
                fichero.guardarEnArchivo(PRESTAMOS_LIBRO, prestamo.toString());
            } else {
                if (!libroEncontrado) {
                    JOptionPane.showMessageDialog(null, "No se encontró un libro con el titulo proporcionado.");
                }
                if (!usuarioEncontrado) {
                    JOptionPane.showMessageDialog(null, "No se encontró un usuario con el nombre proporcionado.");
                }
            }
        });

    }

    public void realizarReserva() {
        executorService.submit(() -> {
            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del usuario:");
            String titulo = JOptionPane.showInputDialog("Ingrese el titulo del libro a reservar:");

            List<String> lineasU = fichero.leerArchivo("usuarios.txt");
            List<String> lineasL = fichero.leerArchivo("catalogo.txt");
            boolean usuarioEncontrado = false;
            boolean libroEncontrado = false;
            Usuario u = null;
            Libro libroSolicitado = null;

            // Encontrar usuario
            for (String linea : lineasU) {
                Usuario usuario = convertirLineaUsuario(linea);
                if (usuario.getNombre().equalsIgnoreCase(nombre)) {
                    usuarioEncontrado = true;
                    u = usuario;
                    break;
                }
            }
            // Encontrar libro
            for (String linea : lineasL) {
                Libro libro = convertirLineaLibro(linea);
                if (libro.getTitulo().equals(titulo)) {
                    libroEncontrado = true;
                    libroSolicitado = libro;
                    break;
                }
            }

            if (usuarioEncontrado && libroEncontrado) {
                estadosLibros.put(libroSolicitado.getID(),"Reservado");
                LocalDate fechaInicio = LocalDate.now();
                LocalDate fechaDevolucion = fechaInicio.plusWeeks(2);
                String nuevoID = String.valueOf(obtenerProximoID(USUARIOS_LOG));
                Prestamo prestamo = new Prestamo(nuevoID, libroSolicitado, u, fechaInicio, fechaDevolucion);
                prestamos.add(prestamo);
                
                actualizarEstadoLibroEnArchivo(libroSolicitado.getID(),"Reservado");

                JOptionPane.showMessageDialog(null, "Su reserva se ha realizado exitosamente.");
                fichero.guardarEnArchivo(RESERVA_LIBRO, prestamo.toString());
            } else {
                if (!libroEncontrado) {
                    JOptionPane.showMessageDialog(null, "No se encontró un libro con el titulo proporcionado.");
                }
                if (!usuarioEncontrado) {
                    JOptionPane.showMessageDialog(null, "No se encontró un usuario con el nombre proporcionado.");
                }
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public Usuario obtenerUsuarioPorNombre(String nombre) {
        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().equalsIgnoreCase(nombre)) {
                return usuario;
            }
        }
        return null;
    }

    public Libro obtenerLibroPorTitulo(String titulo) {
        for (Libro libro : libros) {
            if (libro.getTitulo().equalsIgnoreCase(titulo)) {
                return libro;
            }
        }
        return null;
    }

    private void cargarLibros() {
        List<String> lineas = fichero.leerArchivo(CATALOG_LIBRO);
        for (String linea : lineas) {
            try {
                Libro libro = convertirLineaLibro(linea);
                libros.add(libro);
            } catch (IllegalArgumentException e) {
                System.err.println("Advertencia: " + e.getMessage());
            }
        }
    }

    private void cargarUsuarios() {
        List<String> lineas = fichero.leerArchivo(USUARIOS_LOG);
        for (String linea : lineas) {
            try {
                Usuario usuario = convertirLineaUsuario(linea);
                usuarios.add(usuario);
            } catch (IllegalArgumentException e) {
                System.err.println("Advertencia: " + e.getMessage());
            }
        }
    }

    private static Usuario convertirLineaUsuario(String linea) {
        String[] parts = linea.split(",");

        Usuario usuario = new Usuario();
        usuario.setID(parts[0]);
        usuario.setNombre(parts[1]);
        usuario.setTipoUsuario(parts[2]);
        return usuario;
    }

    private static Libro convertirLineaLibro(String linea) {
        String[] parts = linea.split(",");

        Libro libro = new Libro();
        libro.setID(parts[0]);
        libro.setTitulo(parts[1]);
        libro.setAutor(parts[2]);
        libro.setEstado(parts[3]);
        return libro;
    }

    public int obtenerProximoID(String nombreArchivo) {
        List<String> lineas = fichero.leerArchivo(nombreArchivo);
        int maxID = 0;

        for (String linea : lineas) {
            String[] partes = linea.split(",");
            if (partes.length > 0) {
                int id = Integer.parseInt(partes[0].trim());
                if (id > maxID) {
                    maxID = id;
                }
            }
        }
        return maxID + 1;
    }

    public void mostrarLibrosRegistrados() {
        List<String> lineasLibros = fichero.leerArchivo("catalogo.txt");
        StringBuilder listaLibros = new StringBuilder("Libros registrados:\n");
        for (String linea : lineasLibros) {
            Libro libro = convertirLineaLibro(linea);
            listaLibros.append(libro.getTitulo()).append(" - ").append(libro.getAutor()).append("\n");
        }

        JOptionPane.showMessageDialog(null, listaLibros.toString(), "Lista de Libros", JOptionPane.INFORMATION_MESSAGE);
    }
    private void actualizarEstadoLibroEnArchivo(String libroID, String nuevoEstado) {
    List<String> lineas = fichero.leerArchivo(CATALOG_LIBRO);
    for (int i = 0; i < lineas.size(); i++) {
        Libro libro = convertirLineaLibro(lineas.get(i));
        if (libro.getID().equals(libroID)) {
            libro.setEstado(nuevoEstado);
            lineas.set(i, libro.toString()); 
            break;
        }
    }
    fichero.escribirArchivo(CATALOG_LIBRO, lineas);
}

    public void mostrarHistorialPrestamos() {
        String todasLasLineas = Leer();
        if (todasLasLineas.isEmpty()) {
            todasLasLineas = "No se encontraron préstamos registrados.";
        }
        JOptionPane.showMessageDialog(null, todasLasLineas, "Historial de Préstamos", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String Leer() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("prestamos.txt"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append("Informacion Prestamo").append(linea).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return sb.toString();
    }

   public Prestamo convertirLineaPrestamo(String linea) {
    String[] partes = linea.split(",");

    if (partes.length < 5) {
        System.out.println("Formato incorrecto: " + linea);
        return null; 
    }

    String id = partes[0]; 
    String tituloLibro = partes[1]; 
    String nombreUsuario = partes[2]; 
    LocalDate fechaPrestamo = LocalDate.parse(partes[3]);
    LocalDate fechaDevolucion = LocalDate.parse(partes[4]);

    Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
    if (usuario == null) {
        System.out.println("No se pudo asociar el usuario con el nombre: " + nombreUsuario);
    }

    Libro libro = obtenerLibroPorTitulo(tituloLibro);

    return new Prestamo(id, libro, usuario, fechaPrestamo, fechaDevolucion);
}

public void DevolverLibro() {
    String nombre = JOptionPane.showInputDialog("Ingrese el nombre del usuario que solicitó el préstamo:");
    String titulo = JOptionPane.showInputDialog("Ingrese el título del libro:");

    lock.lock();
    try {
        List<String> lineasPrestamos = fichero.leerArchivo(PRESTAMOS_LIBRO);
        List<String> nuevosPrestamos = new ArrayList<>();
        boolean prestamoEncontrado = false;

        for (String linea : lineasPrestamos) {
            Prestamo prestamo = convertirLineaPrestamo(linea);
            Usuario usuario = prestamo != null ? prestamo.getUsuario() : null;

            if (prestamo != null) {
                if (usuario != null) {
                    String nombreUsuario = usuario.getNombre();

                    if (nombreUsuario.equalsIgnoreCase(nombre) && prestamo.getLibro().getTitulo().equalsIgnoreCase(titulo)) {
                        prestamoEncontrado = true; // Marcar como encontrado
                        JOptionPane.showMessageDialog(null, "El libro ha sido devuelto exitosamente.");
                        actualizarEstadoLibroEnArchivo(prestamo.getLibro().getID(), "DISPONIBLE");
                        continue; 
                    }
                } else {
                    System.out.println("El préstamo no tiene un usuario asociado.");
                }
                nuevosPrestamos.add(linea);
            } else {
                System.out.println("Préstamo no válido: " + linea);
            }
        }

        if (prestamoEncontrado) {
            fichero.escribirArchivo(PRESTAMOS_LIBRO, nuevosPrestamos);
        } else {
            JOptionPane.showMessageDialog(null, "No se encontró un préstamo para el usuario y libro proporcionados.");
        }
    } finally {
        lock.unlock();
    }
}


}

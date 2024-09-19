package app;


import app.Biblioteca;
import app.Biblioteca;
import app.Libro;
import app.Libro;
import app.Usuario;
import app.Usuario;
import javax.swing.JOptionPane;


public class BibliotecaApp {
    private static Biblioteca biblioteca = new Biblioteca();
    
     public static void main(String[] args) {
        while (true) {
            JOptionPane.showMessageDialog(null, "Bienvenido al Sistema de Biblioteca Virtual");
            String opcion = JOptionPane.showInputDialog(
                    "Menu Principal\nPor favor indique la opción que desee:\n"
                    + "1. Agregar Libro\n"
                    + "2. Agregar Usuario\n"
                    + "3. Prestar Libro\n"
                    + "4. Lista Libros\n"
                    + "5. Cargar Historial Prestamos\n"
                    + "6. Reservar Libro\n"
                    + "7. Devolver Libro\n"
                    + "8. Salir"
            );

            if (opcion == null) {
                JOptionPane.showMessageDialog(null, "Operación cancelada.");
                continue;
            }

            switch (opcion) {
                case "1":
                    AgregarLibro();
                    break;
                case "2":
                    AgregarUsuario();
                    break;
                case "3":
                    biblioteca.realizarPrestamo();
                    break;
                case "4":
                    biblioteca.mostrarLibrosRegistrados();
                    break;
                case "5":
                    biblioteca.mostrarHistorialPrestamos();
                    break;
                case "6":
                    biblioteca.realizarReserva();
                    break;
                case "7":
                    biblioteca.DevolverLibro();
                    break;
                case "8":
                    JOptionPane.showMessageDialog(null, "Gracias por utilizar nuestro servicio de Sistema de Biblioteca");
                    return;
                default:
                    JOptionPane.showMessageDialog(null, "Opción no válida.");
            }
        }
    }

    public static void AgregarLibro() {
        String titulo = JOptionPane.showInputDialog("Ingrese el título del libro a ingresar: ");
        String autor = JOptionPane.showInputDialog("Ingrese el autor del libro a ingresar: ");
        String estado = JOptionPane.showInputDialog("Ingrese el estado del libro a ingresar (Disponible/reservado/prestado): ");

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setEstado(estado);

        biblioteca.agregarLibro(libro);
        JOptionPane.showMessageDialog(null, "Libro registrado exitosamente");
    }

    public static void AgregarUsuario() {
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del usuario a ingresar: ");
        String tipoUsuario = JOptionPane.showInputDialog("Ingrese el tipo de Usuario (normal,premium)");

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setTipoUsuario(tipoUsuario);

        biblioteca.agregarUsuario(usuario);
        JOptionPane.showMessageDialog(null, "El usuario ha sido registrado exitosamente");
    }

    }

       
            

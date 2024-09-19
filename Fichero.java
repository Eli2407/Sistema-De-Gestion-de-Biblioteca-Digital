package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Fichero {

    public void crearArchivo(String fileName, String contenido) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(contenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     
    public List<String> leerArchivo(String archivo) {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineas;
    }

    public void escribirArchivo(String archivo, List<String> lineas) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    public void guardarEnArchivo(String nombreArchivo, String contenido){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo,true))){
            bw.write(contenido);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




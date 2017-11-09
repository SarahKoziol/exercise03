package org.geotools.tutorial.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * This example reads data for point locations and associated attributes from a comma separated text
 * (CSV) file and exports them as a new shapefile. It illustrates how to build a feature type.
 * <p>
 * Note: to keep things simple in the code below the input file should not have additional spaces or
 * tabs between fields.
 */
public class Csv2Shape {

    public static void main(String[] args) throws Exception {

        File file = JFileDataStoreChooser.showOpenFile("csv", null); //evtl Lösung: http://docs.geotools.org/latest/javadocs/org/geotools/swing/data/JFileDataStoreChooser.html
        if (file == null) {
            return;
        }

        /*
         * We use the DataUtilities class to create a FeatureType that will describe the data in our
         * shapefile.
         *
         * See also the createFeatureType method below for another, more flexible approach.
         */
        Object collection;
        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                "location:Point:srid=4326," + // <- the geometry attribute: Point type
                        "name:String," + // <- a String attribute
                        "number:Integer" // a number attribute

       /*
         * We create a FeatureCollection into which we will put each Feature created from a record
         * in the input csv data file
         */
                SimpleFeatureCollection collection = FeatureCollections.newCollection();
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature (a Point
         * object for the location)
         */
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            /* First line of the data file is the header */
            String line = reader.readLine();
            System.out.println("Header: " + line);

            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0) { // skip blank lines
                    String tokens[] = line.split("\\,");

                    double latitude = Double.parseDouble(tokens[0]);
                    double longitude = Double.parseDouble(tokens[1]);
                    String name = tokens[2].trim();
                    int number = Integer.parseInt(tokens[3].trim());

                    /* Longitude (= x coord) first ! */
                    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

                    featureBuilder.add(point);
                    featureBuilder.add(name);
                    featureBuilder.add(number);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    featureBuilder.add();
                }
            }
        } finally {
            reader.close();

            /*
         * Get an output file name and create the new shapefile
         */
            File newFile = getNewShapeFile(file);

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(TYPE);

        /*
         * You can comment out this line if you are using the createFeatureType method (at end of
         * class file) rather than DataUtilities.createType
         */
            newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);


    //private static void getNewShapeFile() {
    //}
         /*
         * Write the features to the shapefile
         */
            Transaction transaction = new DefaultTransaction("create");

            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

                featureStore.setTransaction(transaction);
                try {
                    featureStore.addFeatures(collection);
                    transaction.commit();

                } catch (Exception problem) {
                    problem.printStackTrace();
                    transaction.rollback();

                } finally {
                    transaction.close();
                }
                System.exit(0); // success!
            } else {
                System.out.println(typeName + " does not support read/write access");
                System.exit(1);
            }
            /**
             * Prompt the user for the name and path to use for the output shapefile
             *
             * @param csvFile
             *            the input csv file used to create a default shapefile name
             *
             * @return name and path for the shapefile as a new File object
             */
            private static File getNewShapeFile(File Object csvFile;
            csvFile) {
                String path = csvFile.getAbsolutePath();
                String newPath = path.substring(0, path.length() - 4) + ".shp";

                JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
                chooser.setDialogTitle("Save shapefile");
                chooser.setSelectedFile(new File(newPath));

                int returnVal = chooser.showSaveDialog(null);

                if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
                    // the user cancelled the dialog
                    System.exit(0);
                }

                File newFile = chooser.getSelectedFile();
                if (newFile.equals(csvFile)) {
                    System.out.println("Error: cannot replace " + csvFile);
                    System.exit(0);
                }

                return newFile;
            }
        }
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
        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                "location:Point:srid=4326," + // <- the geometry attribute: Point type
                        "name:String," + // <- a String attribute
                        "number:Integer" // a number attribute
        );


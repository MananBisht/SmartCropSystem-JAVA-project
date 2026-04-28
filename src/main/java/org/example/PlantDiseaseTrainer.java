package org.example;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.datavec.api.io.filters.BalancedPathFilter;

import java.io.File;
import java.util.Random;

public class PlantDiseaseTrainer {

    public static void main(String[] args) throws Exception {

        int height = 160;
        int width = 160;
        int epochs = 8;
        int batchSize = 16;
        int channels = 3 ;

        File dataDir = new File("plantvillage dataset/color");
        FileSplit fileSplit = new FileSplit(dataDir, NativeImageLoader.ALLOWED_FORMATS, new Random(42));
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

        ImageRecordReader tempReader = new ImageRecordReader(height, width, channels, labelMaker);
        tempReader.initialize(fileSplit);

        int numClasses = tempReader.getLabels().size();

// NOW create path filter
        BalancedPathFilter pathFilter = new BalancedPathFilter(
                new Random(42),
                NativeImageLoader.ALLOWED_FORMATS,
                labelMaker
        );
        // 80% train, 20% test
        InputSplit[] splits = fileSplit.sample(pathFilter, 80, 20);
        InputSplit trainData = splits[0];
        InputSplit testData = splits[1];

        ImageRecordReader trainReader = new ImageRecordReader(height, width, channels, labelMaker);
        trainReader.initialize(trainData);
        numClasses = trainReader.getLabels().size();

        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainReader, batchSize, 1, numClasses);
        
        // Image Normalization
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        trainIter.setPreProcessor(scaler);

        ImageRecordReader testReader = new ImageRecordReader(height, width, channels, labelMaker);
        testReader.initialize(testData);
        DataSetIterator testIter = new RecordReaderDataSetIterator(testReader, batchSize, 1, numClasses);
        testIter.setPreProcessor(scaler);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(42)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()

                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nIn(channels)
                        .nOut(32)
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())

                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .build())

                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())

                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())

                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .build())

                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .build())

                .layer(new org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer.Builder()
                        .poolingType(org.deeplearning4j.nn.conf.layers.PoolingType.AVG)
                        .build())

                .layer(new DenseLayer.Builder()
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numClasses)
                        .activation(Activation.SOFTMAX)
                        .build())

                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        model.setListeners(new ScoreIterationListener(10));

        System.out.println("Training started...");

        for (int i = 0; i < epochs; i++) {
            trainIter.reset();
            model.fit(trainIter);
            System.out.println("Epoch " + (i + 1) + " training complete");

            System.out.println("Evaluating model on test data...");
            testIter.reset();
            Evaluation eval = model.evaluate(testIter);
            System.out.println(eval.stats());
        }

        System.out.println("Training and Evaluation complete!");

        // Save model
        File modelFile = new File("plant_disease_model.zip");
        org.deeplearning4j.util.ModelSerializer.writeModel(model, modelFile, true);

        System.out.println("Model saved!");
    }
}
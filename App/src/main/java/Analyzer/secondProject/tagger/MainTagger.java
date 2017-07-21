package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.info.InfoContainer;
import Analyzer.repository.TagRepository;
import com.sun.media.sound.InvalidDataException;
import Analyzer.secondProject.csv.reader.*;
import Analyzer.secondProject.csv.writer.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

//../../ -> ../SecondProject
//./ -> ../SecondProject/Projekt-IO01/FeedsAnalyzer-master
public class MainTagger {

	private static TagRepository tagRepository;

	public static final String DEFAULT_CONTENT = "NO CONTENT";

	private static final String[] tagFiles = {InfoContainer.countryTagFile, InfoContainer.ORGANIZATION_TAG_FILE_PATH, InfoContainer.ORGANIZATION_SHORT_TAG_FILE_PATH};
	private static final String[] destinationSuffixes = {"taggedForCountry", "taggedForOrg", "taggedForOrg/SHORT"};

	private static final String destinationCurrencyTagSuffix = "taggedForCurrency";
	private static  CurrencyTagger[] currencyTaggers = new CurrencyTagger[2];

	/*public static void main(String[] args) throws IOException {
		final long denominator = 1000000000;
		if (tagFiles.length != destinationSuffixes.length) {
			System.err.println("Error in paths");
			System.exit(1);
		}

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
		Path directory = Paths.get(DESTINATION_TAGS_FOLDER_PATHS);
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (NoSuchFileException e) {
			System.out.println("Directory for tagged feeds didn't exist");
		}
		long startTime = System.nanoTime();

		tagNewFeedsCurrency(0);
		//tagNewFeeds(1);
		long firstLoopTime = System.nanoTime();
		//tagGeomedia(2);
		long secondLoopTime = System.nanoTime();
		System.out.println("Finished");
		System.out.println("First time: " + ((firstLoopTime - startTime) / denominator));
		System.out.println("Second time: " + ((secondLoopTime - firstLoopTime) / denominator));
	}*/

	public static void initializeMainTagger() throws IOException {
		currencyTaggers[0] = new PolishCurrencyTagger("Polish");
		currencyTaggers[1] = new EnglishCurrencyTagger("English");

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
		Path directory = Paths.get(InfoContainer.DESTINATION_TAGS_FOLDER_PATHS);
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (NoSuchFileException e) {
			System.out.println("Directory for tagged feeds didn't exist");
		}
	}

	public static void tagNewFeeds(int workId) throws IOException {
		long startTime = System.nanoTime();
		for (int i=0; i<tagFiles.length; ++i) {
			System.out.println("Work " + workId + "." + i);
			tagFileNormalWay(tagFiles[i], InfoContainer.NEW_FEEDS_PATH, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationSuffixes[i]);
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagGeomedia(int workId) throws IOException {
		long startTime = System.nanoTime();
		for (int i=0; i<tagFiles.length; ++i) {
            System.out.println("Work " + workId + "." + i);
            tagFileWithUniqueInName(tagFiles[i], InfoContainer.oldFeedsFolderPaths, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationSuffixes[i]);
        }
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagNewFeedsCurrency(int workId) {
		if (InfoContainer.currencyTagFiles.length != currencyTaggers.length) {
			System.err.println("Error in start of currency, lengths: " + InfoContainer.currencyTagFiles.length + ", " + currencyTaggers.length);
			return;
		}
		long startTime = System.nanoTime();
		for (int i=0; i<currencyTaggers.length; ++i) {
			try {
				System.out.println("Work " + workId + "." + i);
				currencyTaggers[i].work(InfoContainer.currencyTagFiles[i], InfoContainer.NEW_FEEDS_PATH, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationCurrencyTagSuffix);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagGeomediaCurrency(int workId) {
		if (InfoContainer.currencyTagFiles.length != currencyTaggers.length) {
			System.err.println("Error in start of currency, lengths: " + InfoContainer.currencyTagFiles.length + ", " + currencyTaggers.length);
			return;
		}
		long startTime = System.nanoTime();
		for (int i=0; i<currencyTaggers.length; ++i) {
			try {
				currencyTaggers[i].work(InfoContainer.currencyTagFiles[i], InfoContainer.oldFeedsFolderPaths, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationCurrencyTagSuffix);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	private static void tagFileNormalWay(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null files for " + sourceFolderPath);
			return;
		}

		Set<ComplexTag> complexTags = null;

		for (File f: files){
			if (f.isFile()) {
				String sourceFilePath = f.getAbsolutePath();
				String destinationFilePath = destinationFolderPath + "/" + f.getName();
				int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

				List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
				List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
				List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
				List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

				if (complexTags == null) {
					complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, dataPositions[4], dataPositions[5]);
				}

				System.out.println(sourceFilePath + " is being tagged");
				TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath);
				tagFile(tagDataContainer);
			}
		}



	}
	private static void tagFileWithUniqueInName(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		String[] directories = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (directories == null) {
			System.out.println("No directories in " + file.getAbsolutePath());
			return;
		}
		Set<ComplexTag> complexTags = null;

		for (String dir: directories){
			String sourceFilePath = sourceFolderPath + "/" + dir + "/rss_unique.csv";
			String destinationFilePath = destinationFolderPath + "/" + dir + ".csv";

			int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

			List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
			List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
			List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
			List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

			if (complexTags == null) {
				complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, dataPositions[4], dataPositions[5]);
			}

			System.out.println(sourceFilePath + " is being tagged");
			TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath);
			tagFile(tagDataContainer);
		}

	}



	private static void tagFile(TagDataContainer tagDataContainer) throws IOException {

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			String title = tagDataContainer.getTitles().get(i).toLowerCase();
			String description = tagDataContainer.getDescriptions().get(i).toLowerCase();

			for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
				for (String keyword: complexTag.getKeyWords()) {
					try {
						if ((title.contains(keyword)) || (description.contains(keyword))) {
							WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
							break;
						}
					} catch (IndexOutOfBoundsException e) {
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
					}
				}
			}
		}
	}

	private static void createTagsAndCountriesFile() throws IOException {
		String resultFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagDataContainer.getTags()AndCountries.csv";
		String tagsFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv";
		String countriesFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/countries.csv";
		Map<String,String> tagsAndCountries = ReaderCsvFiles.readTwoFilesAndReturnTagsWithCountries(tagsFilePath,countriesFilePath);
		WriterCsvFiles.writeTagsAndCountries(resultFilePath, tagsAndCountries);
	}

	public static int[] getDataPositions(String sourceFilePath, String tagsFilePath) throws IOException {
		int[] toReturn = new int[6];
		char[] readBytes = new char[4];
		String readBytesString;

		FileReader tmpFileReader = new FileReader(sourceFilePath);
		if (tmpFileReader.read(readBytes, 0, 4) != 4) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			throw new InvalidDataException();
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("\"ID\"")) {
			toReturn[0] = 1;
			toReturn[1] = 2;
			toReturn[2] = 3;
			toReturn[3] = 4;
		} else {
			toReturn[0] = 0;
			toReturn[1] = 1;
			toReturn[2] = 2;
			toReturn[3] = 3;
		}

		readBytes = new char[2];
		tmpFileReader = new FileReader(tagsFilePath);
		if (tmpFileReader.read(readBytes, 0, 2) != 2) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			throw new InvalidDataException();
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("ID")) {
			toReturn[4] = 1;
			toReturn[5] = 2;
		} else {
			toReturn[4] = 0;
			toReturn[5] = 1;
		}
		return toReturn;
	}

	private static Set<CurrencyTag> getCurrencyTags(){
		return new HashSet<>();
	}

	public static TagRepository getTagRepository() {
		return tagRepository;
	}

	public static void setTagRepository(TagRepository tagRepository) {
		MainTagger.tagRepository = tagRepository;
	}

}
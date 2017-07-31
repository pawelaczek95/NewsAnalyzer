package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import au.com.bytecode.opencsv.CSVReader;
import com.sun.media.sound.InvalidDataException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static Analyzer.secondProject.tagger.MainTagger.getSourceDataPositions;
import static Analyzer.secondProject.tagger.MainTagger.getTagsDataPositions;

public class EnglishCurrencyTagger extends CurrencyTagger {


	public EnglishCurrencyTagger() {
		super("English");
	}

	@Override
	protected Set<ComplexTag> getComplexTags(String tagFilePath) throws IOException {
		char separator = ReaderCsvFiles.getSeparator(tagFilePath);
		Set<ComplexTag> result = null;

		try (FileReader fileReader = new FileReader(tagFilePath)) {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = new HashSet<>();
			String[] nextLine = reader.readNext();

			while (nextLine != null) {
				String[] keyWords = nextLine[1].split(BasicTagger.REGEX);
				ComplexTag complexTag = new CurrencyTag(nextLine[0], keyWords[keyWords.length-1].toLowerCase());
				for (int i=keyWords.length-2; i>=0; --i) {
					complexTag.keyWords.add(keyWords[i].toLowerCase());
				}
				result.add(complexTag);
				nextLine = reader.readNext();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


}

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.io.*;

class LZCore {

    // ascii codes size
    int dictSize = 256;
    Map <String, String> dictionary;

    void populateDictionaryWithExtWords(Map <String, String> dictionary) {
        try {
            File myObj = new File("10000-words-en.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                dictionary.put(data, Integer.toString(dictSize++));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred when reading words file.");
            e.printStackTrace();
        }
        System.out.println("Curent dictionary: " + dictionary);
    }

    List<String> encodeParallely(String s,
                                  int beginIndex,
                                  int endIndex) {
        String[] substr = s.substring(beginIndex, endIndex).split(" ");
        List<String> result = new ArrayList<>();
        for (String sub : substr) {
//            System.out.println(dictionary.getOrDefault(sub, "*************************"));
            result.add(dictionary.getOrDefault(sub, sub));
        }
        return result;
    }

    List<String> encode(String s) {
        dictionary = new HashMap<>();

        for (int i = 0; i < dictSize; i++)
            dictionary.put(String.valueOf((char) i), Integer.toString(i));

        populateDictionaryWithExtWords(dictionary);
        System.out.println("Dictsize now: " + dictSize);
        List<String> result = new ArrayList<>();

        int n = s.length();
        // use 4 thread
        CompletableFuture thread1 = CompletableFuture.runAsync(() -> {
            result.addAll(encodeParallely(s, 0 ,n/4));
        });

        CompletableFuture thread2 = CompletableFuture.runAsync(() -> {
            result.addAll(encodeParallely(s, n/4, n/2));
        });

        CompletableFuture thread3 = CompletableFuture.runAsync(() -> {
            result.addAll(encodeParallely(s, n/2, (int) (n/1.33)));
        });

        CompletableFuture thread4 = CompletableFuture.runAsync(() -> {
            result.addAll(encodeParallely(s, (int) (n/1.33), n));
        });
        // process each chunk seperately
        // combine result from each thread

        while (true) {
            if (thread1.isDone() && thread2.isDone() && thread3.isDone() && thread4.isDone()) break;
        }

        // return result;
        System.out.println("Encoded: " + result.stream().map(String::valueOf).collect(Collectors.joining(" ")));

        return result;
    }

    String decode(List<Integer> encodedText) {
        Map <Integer, String> dictionary = new HashMap<>();

        for (int i = 0; i < dictSize; i++)
            dictionary.put(i, String.valueOf((char) i));

        String characters = String.valueOf((char) encodedText.remove(0).intValue());
        StringBuilder result = new StringBuilder(characters);
        for (int code : encodedText) {
            String entry = dictionary.containsKey(code) ?
                    dictionary.get(code) : characters + characters.charAt(0);
            result.append(entry);
            dictionary.put(dictSize++, characters + entry.charAt(0));
            characters = entry;
        }

        return result.toString();
    }
}

public class Main {
    public static void main(String args[]) {
        long startTime = System.nanoTime();
        String s = "";

        try {
            File myObj = new File("input_long.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                s += data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        LZCore lzcore = new LZCore();
        System.out.println(s);
        List<String> encoded = lzcore.encode(s);
//        System.out.println(lzcore.decode(encoded));

        try {
            FileWriter myWriter = new FileWriter("output_long.txt");
            myWriter.write(encoded.stream().map(String::valueOf).collect(Collectors.joining(" ")));
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println("Final time : " + totalTime/1000000);
    }
}
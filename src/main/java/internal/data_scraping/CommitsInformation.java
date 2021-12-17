package main.java.internal.data_scraping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;


import main.java.internal.utils.JSONConfig;
import main.java.internal.entities.commits_entities.CommitInfo;
import main.java.internal.utils.DownloaderAgent;

public class CommitsInformation {

    private static final Logger LOGGER = Logger.getLogger(CommitsInformation.class.getName());

    private CommitInfo[] commitsInfo;


    private static final String TIMEOUT_MSG = "'timeout occurred";

    private static CommitsInformation instance = null;

    public static CommitsInformation getInstance() {
        if (instance == null) {
            instance = new CommitsInformation();
        }
        return instance;
    }

    /**
     *
     */
    private CommitsInformation() {
        super();
        commitsInfo = new CommitInfo[0];
    }

    private void downloadCommits(String url, int tokenIndex, String cachePath, int j, String[] list){
        String reString = null;
        try {
            reString = DownloaderAgent.readJsonFromGitHub(url, tokenIndex, cachePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fillJsonList(reString, j, list);

    }

    public CommitInfo[] retrieveCommits(String queryString) throws InterruptedException {
        /* Get project name */
        int requestPerThread = 30;
        String project = JSONConfig.getProjectName();
        int currpage = 1;
        boolean endCondition;
        Gson gson = new Gson();
        List<CommitInfo> listOfAllCommit = new ArrayList<>();
        do {
            final int tokenIndex = JSONConfig.getPseudoRandomIndex();
            String[] list = new String[requestPerThread];
            ExecutorService es = Executors.newCachedThreadPool();
            for (int i = 0; i < requestPerThread; i++) {
                String url = JSONConfig.getRepository() + project.toLowerCase(Locale.ROOT) + "/commits?page=" + currpage
                        + "&per_page=100" + queryString;
                final int j = i;
                es.execute(() -> downloadCommits(url, tokenIndex,"cache/" , j, list));
                currpage++;

            }
            es.shutdown();
            if (!es.awaitTermination(1, TimeUnit.MINUTES)) {
                LOGGER.log(Level.WARNING, TIMEOUT_MSG);
            }

            endCondition = false;
            for (String jsonString : list) {
                if (!jsonString.equals("[]")) {// update commits list
                    CommitInfo[] commitList = gson.fromJson(jsonString, CommitInfo[].class);
                    listOfAllCommit.addAll(Arrays.asList(commitList));
                    if (commitList.length < 100) {
                        // the next page will be empty
                        endCondition = true;
                    }
                } else {
                    endCondition = true;
                }
            }
        } while (!endCondition);

        sortCommitInfos(listOfAllCommit);
        return listOfAllCommit.toArray(new CommitInfo[listOfAllCommit.size()]);

    }

    public CommitInfo[] getCommitsInfo() throws InterruptedException {
        if (commitsInfo.length == 0) {
            this.commitsInfo = retrieveCommits("");
        }
        return commitsInfo;
    }

    public static LocalDate formatCommitDate(String dateStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateStr = dateStr.replace("T", " ").replace("Z", "");
        return LocalDate.parse(dateStr, dateTimeFormatter);
    }

    private static void fillJsonList(String json, int index, String[] list) {
        list[index] = json;
    }


    private void sortCommitInfos(List<CommitInfo> infos) {

        Collections.sort(infos, (CommitInfo c1, CommitInfo c2) -> {
            LocalDate c1Date = formatCommitDate(c1.getCommit().getCommitter().getDate());
            LocalDate c2Date = formatCommitDate(c2.getCommit().getCommitter().getDate());
            if (c1Date.isBefore(c2Date)) {
                return 1;
            } else if (c2Date.isBefore(c1Date)) {
                return -1;
            } else {
                return 0;
            }

        });

    }
}

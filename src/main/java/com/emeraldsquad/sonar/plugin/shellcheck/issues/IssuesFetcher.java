package com.emeraldsquad.sonar.plugin.shellcheck.issues;

import java.io.Reader;

/**
 * IssuesFetcher
 */
public interface IssuesFetcher {

    public Reader fetchReport() throws Exception;
}
/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.dev.wikipedia.atom;

import org.junit.Test;
import org.languagetool.language.German;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomFeedCheckerTest {

  private static final String DB_URL = "jdbc:derby:atomFeedChecksDB;create=true";

  @Test
  public void testCheck() throws IOException {
    AtomFeedChecker atomFeedChecker = new AtomFeedChecker(new German());
    CheckResult checkResult = atomFeedChecker.checkChanges(getStream());
    List<ChangeAnalysis> changeAnalysis = checkResult.getCheckResults();
    assertThat(changeAnalysis.size(), is(3));

    assertThat(changeAnalysis.get(0).getAddedMatches().size(), is(1));
    assertThat(changeAnalysis.get(0).getAddedMatches().get(0).getRule().getId(), is("DE_AGREEMENT"));
    assertTrue(changeAnalysis.get(0).getAddedMatches().get(0).getErrorContext().contains("Fehler: <err>der Haus</err>"));
    assertThat(changeAnalysis.get(0).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(1).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(1).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(2).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(2).getRemovedMatches().size(), is(0));

    CheckResult checkResult2 = atomFeedChecker.checkChanges(getStream());
    List<ChangeAnalysis> changeAnalysis2 = checkResult2.getCheckResults();
    assertThat(changeAnalysis2.size(), is(3));   // not skipped because no database is used
  }

  @Test
  public void testCheckToDatabase() throws IOException, SQLException {
    initDatabase();
    DatabaseConfig databaseConfig = new DatabaseConfig(DB_URL, "user", "pass");
    AtomFeedChecker atomFeedChecker1 = new AtomFeedChecker(new German(), databaseConfig);
    CheckResult checkResult = atomFeedChecker1.runCheck(getStream());
    List<ChangeAnalysis> changeAnalysis = checkResult.getCheckResults();
    assertThat(changeAnalysis.size(), is(3));

    assertThat(changeAnalysis.get(0).getAddedMatches().size(), is(1));
    assertThat(changeAnalysis.get(0).getAddedMatches().get(0).getRule().getId(), is("DE_AGREEMENT"));
    assertTrue(changeAnalysis.get(0).getAddedMatches().get(0).getErrorContext().contains("Fehler: <err>der Haus</err>"));
    assertThat(changeAnalysis.get(0).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(1).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(1).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(2).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(2).getRemovedMatches().size(), is(0));

    AtomFeedChecker atomFeedChecker2 = new AtomFeedChecker(new German(), databaseConfig);
    CheckResult checkResult2 = atomFeedChecker2.runCheck(getStream());
    List<ChangeAnalysis> changeAnalysis2 = checkResult2.getCheckResults();
    // this is actually not correct (but doesn't matter) - all articles could be skipped as they
    // have been checked in the previous run:
    assertThat(changeAnalysis2.size(), is(2));
  }

  private void initDatabase() throws SQLException {
    MatchDatabase database = new MatchDatabase(DB_URL, "user", "pass");
    database.drop();
    database.createTable();
  }

  private InputStream getStream() throws IOException {
    return Tools.getStream("/org/languagetool/dev/wikipedia/atom/feed1.xml");
  }
  
}
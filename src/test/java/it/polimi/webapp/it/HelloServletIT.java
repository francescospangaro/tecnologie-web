/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package it.polimi.webapp.it;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/** Tests the Web application, by checking that the index page returns a code 200 */
public class HelloServletIT {

    private static final String BASE_URL = "http://localhost:" + System.getProperty("servlet.port") + "/webapp";

    /** Call the servlet page */
    @Test
    @Disabled // It cannot be fixed as we don't have any db to connect to on gh actions
    public void callIndexPage() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assertions.assertEquals(200, connection.getResponseCode());
    }
}
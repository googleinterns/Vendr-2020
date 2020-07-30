// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.servlets.authstatus.AuthStatus;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet to handle the user's log status */
@WebServlet("/log-status")
public class AuthServlet extends HttpServlet {

  public static final String REDIRECT_URL = "/";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json;");

    AuthStatus authStatus = new AuthStatus();

    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      // Redirect to the portfolio after log out
      String logoutUrl = userService.createLogoutURL(REDIRECT_URL);

      authStatus.setUrl(logoutUrl);
      authStatus.setLoggedInStatus(true);

      response.getWriter().println(new Gson().toJson(authStatus));
    } else {
      // Redirect to the portfolio after log in
      String loginUrl = userService.createLoginURL(REDIRECT_URL);

      authStatus.setUrl(loginUrl);
      authStatus.setLoggedInStatus(false);

      response.getWriter().println(new Gson().toJson(authStatus));
    }
  }
}

// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/**
 * File to handle Google's Auth API requests
 */

function getLogStatus() {
  fetch('/log-status').then(response => response.json()).then((logStatus) => {
    setLogURLInNavBar(logStatus);
  });
}

function setLogURLInNavBar(logStatus) {
  const logButton = document.getElementById('log_button');
  const logForm = document.getElementById('log_submit_form');

  logButton.innerText = (logStatus.isLogged) ? 'Log Out' : 'Log In';
  logButton.href = logStatus.url;
}

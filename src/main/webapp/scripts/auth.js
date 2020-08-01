// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


/**
 * File to handle Google's Auth API requests
 */

const PHONE_NUMBER_LENGTH = 10;

function getLogStatus() {
  fetch('/log-status').then(response => response.json()).then((logStatus) => {
    if(!logStatus.isRegistered && logStatus.isLogged) {
      redirectToRegistrationForm();
    }

    setLogURLInNavBar(logStatus);
  });
}

function setLogURLInNavBar(logStatus) {
  const logButton = document.getElementById('log_button');
  const logForm = document.getElementById('log_submit_form');

  logButton.innerText = (logStatus.isLogged) ? 'Log Out' : 'Log In';
  logButton.href = logStatus.url;
}

function redirectToRegistrationForm() {
 window.location.replace('./registration.html');
}

function validateRegistrationFormInputs() {
  const firstName = document.getElementById('first_name').value;
  const lastName = document.getElementById('last_name').value;
  const phoneNumber = document.getElementById('phone_number').value;

  if(!isValid(firstName) || !isValid(lastName) || !firstName || !lastName) {
    alert('Names can\'t be empty or have special characters');
    return;
  }

  if(phoneNumber.length > PHONE_NUMBER_LENGTH || !phoneNumber) {
    alert(`Phone Number can\'t be empty or have more than ${PHONE_NUMBER_LENGTH} numbers`);
    return;
  }

  handleRegistration(firstName, lastName, phoneNumber);
}

function isValid(name) {
   //Regex for Valid Characters i.e. Alphabets, Numbers and Space.
    const validCharacters = /^[A-Za-z0-9 ]+$/;
    return validCharacters.test(name);
}

function handleRegistration(firstName, lastName, phoneNumber) {
  fetch(`/new-vendor?first_name=${firstName}&last_name=${lastName}&phone_number=${phoneNumber}`, {method: 'POST'})
  .then(response => {
    if(response.redirected) {
      window.location.replace('./home.html');
      return;
    }
    response.text().then((error) => {
      alert(error);
    });
  });
}

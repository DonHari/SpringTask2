import hello.contacts.Application;
import hello.contacts.ContactsController;
import hello.contacts.model.Contact;
import hello.contacts.repository.ContactRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Before;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TestClass {

    private MockMvc mockMvc;

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactsController contactsController = new ContactsController(contactRepository);

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(contactsController).build();
    }

    @Test
    public void testWithoutParameter() throws Exception {
        mockMvc.perform(get("/hello/contacts"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }

    @Test
    public void testWithoutParameterValue() throws Exception {
        mockMvc.perform(get("/hello/contacts?nameFilter"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }

    @Test
    public void testWithParameter() throws Exception {
        String regex = "^.*[0-8].*$";//без цифр от 0 до 8
        String url = "/hello/contacts?nameFilter=" + URLEncoder.encode(regex,"UTF-8");

        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(1, "contact1"));
        contacts.add(new Contact(2, "contact123"));
        contacts.add(new Contact(3, "contact92"));
        contacts.add(new Contact(4, "contact9"));//correct
        contacts.add(new Contact(5, "contact909"));

        Mockito.when(contactRepository.streamAll())
                .thenReturn(contacts.stream());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0]['id']", is(4)))
                .andExpect(jsonPath("$[0]['name']", is("contact9")));

    }
}

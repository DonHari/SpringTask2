package hello.contacts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import hello.contacts.model.Contact;
import hello.contacts.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

@RestController
@EnableAutoConfiguration
public class ContactsController {


    private ContactRepository contactRepository;

//    @PersistenceContext
//    EntityManager entityManager;

    @Autowired
    public ContactsController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @RequestMapping("/hello/contacts")
    @Transactional(readOnly = true)//только чтение из БД
    public void greeting(
            @RequestParam(value="nameFilter",
                    required = false//чтобы обработать ошибку, когда параметр не указан (либо указан, но пустой)
            ) String nameFilter,
            HttpServletResponse response) {
        //проверяем заполненность параметра
        if(nameFilter == null || nameFilter.isEmpty()) {
            throw new IllegalArgumentException();//handleBadRequest
        }

        //на случай, если регулярное выражение не пришло уже декодированным (например, "%5E.*[0-8].*$")
        //если не закодировано, то ничего не изменится
        String regex = null;
        try {
            regex = URLDecoder.decode(nameFilter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //handleInternalServerError
        }

        //если рег. выраж. неправильно записано - выбросит исключение
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            //handlePatternSyntaxException
        }

        try(Stream<Contact> contactStream = contactRepository.streamAll()) {
            response.addHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.OK.value());

            OutputStream out = response.getOutputStream();

            //потоковое отправление ответа
            Gson gson = new GsonBuilder().create();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.setIndent("\t");
            writer.beginArray();
            String finalRegex = regex;
            contactStream.forEach(contact -> {
                if(!contact.getName().matches(finalRegex)) {//обработка регулярного выражение
                    gson.toJson(contact, Contact.class, writer);
                }
            });
            writer.endArray();
            writer.close();
        } catch (IOException e) {
            //handleInternalServerError
        }
    }

    //code 400
    @ExceptionHandler(IllegalArgumentException.class)
    void handleBadRequest(HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Parameter \"nameFilter\" is required!");
    }

    //code 400
    @ExceptionHandler(PatternSyntaxException.class)
    void handlePatternSyntaxException(HttpServletResponse response) throws IOException{
        response.setHeader("Content-type", MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Regular expression incorrect!");
    }

    //code 500
    @ExceptionHandler({IOException.class, UnsupportedEncodingException.class})
    void handleInternalServerError(HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.");
    }

}

package hello;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import javax.servlet.http.HttpSession;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.*;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import static org.springframework.util.StringUtils.isEmpty;

@RestController
public class HelloController{
    private static final String AVAILABLE_BOOKS = "src\\main\\java\\hello\\AvailableBooks.txt";
    private static final String EXISTED_USERS = "src\\main\\java\\hello\\existedusers.txt";
    private static ArrayList<Book> books = new ArrayList<>();
    private static Multimap<String,String> UsersToBooks = ArrayListMultimap.create();
    {try {
        File f = new File(AVAILABLE_BOOKS);
        BufferedReader b = new BufferedReader(new FileReader(f));
        String line;
        String[] arr;
        int FirstElement;
        Boolean SecondElement;
        String ThirdElement;
        int ForthElement;

        //fill the books list
        while ((line = b.readLine()) != null) {
            arr = line.split(" ");
            try{
                FirstElement = Integer.parseInt(arr[0]);
                SecondElement = Boolean.valueOf(arr[1]);
                ThirdElement = arr[2];
                ForthElement = Integer.parseInt(arr[3]);
                books.add(new Book(FirstElement,SecondElement,ThirdElement,ForthElement));
            }
            catch (NumberFormatException ex){ex.getStackTrace();}
        }
        b.close();
    } catch (IOException e){e.printStackTrace();}
        //fill the UsersToBooks multimap
        try(BufferedReader reader = new BufferedReader(new FileReader(EXISTED_USERS)))
        {
            String line = null;
            String [] arr;
            while((line = reader.readLine())!= null)
            {
                arr = line.split(" ");
                if (arr.length>2)
                {
                    for(int i=2; i<arr.length; i++)
                    {
                        UsersToBooks.put(arr[0],arr[i]);
                    }
                }
            }
        }
        catch (IOException e){e.printStackTrace();}
    }

    @RequestMapping("/")
    public String index(HttpSession session) {
        String message = "<html>" +
                "<body>" +
                "<h2> login form </h2>" +
                "<form action='/login'>"+
                "<label><b>Username:</b></label>"+
                "<input type='text' placeholder='Enter it here' name='usr' required>"+
                "<br>"+
                "<button type='submit' name = 'login'>login</button>" +
                "</form>"+
                "</body>" +
                "</html>";
        return (message);
    }

    @RequestMapping("/login")
    public String login(@RequestParam("usr") String name, HttpSession session) throws IOException {
        session.setAttribute("User", name);
        String line;
        String[] arr;
        boolean flag = false;

        //check if the user exists already
        try {
            File f = new File(EXISTED_USERS);
            BufferedReader b = new BufferedReader(new FileReader(f));
            line = "";
            while ((line = b.readLine()) != null) {
                arr = line.split(" ");
                if (arr[0].equals(name)) {
                    flag = true;
                }
            }
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //insert the user if they are new
        if (!flag) {
            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                File file = new File(EXISTED_USERS);

                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                //insert 0 to describe that he has not rent yet
                bw.write(name + " 0" + "\n");


            } catch (IOException e) {

                e.printStackTrace();
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    //LOGGER.log("content",ex);

                }
            }
        }
        String message = "<html>" +
                "<body>" +
                "<h2>Welcome" + " " + name + "</h2>" +
                "<b>Please select an option</b>" +
                "<br><br>" +
                "<a href='/login/BookList'>" +
                "Observe the List" +
                "<br><br>" +
                "</a>";

        //an exei daneistei toulaxiston ena vivlio
        if (UsersToBooks.get(name).size()>0) {
            message += "<a href='/login/UserBooks'>" +
                    "Observe your books" +
                    "</a>" +
                    "<a href='login/Return'>" +
                    "<br><br>Make a return" +
                    "<br><br><a href='/login?usr=" + name + "' login='" + session + "'>HOME</a>" +
                    "</body>" +
                    "</html>";

            //an einai neos
        } else
        {
            message += "<a href='/login?usr=" + name + "' login='" + session + "'>HOME</a>" +
                    "</body>" +
                    "</html>";

        }
        return (message);
    }

    @RequestMapping("/login/Return")
    public String Return(HttpSession session)
    {
        String name = (String) session.getAttribute("User");
        String message = "<html><body><h3>Please give me the ISBN of the book you want to return.</h3>";
        message+="<br><form action='/login/ReturnProccess'>"+
                "<label>ISBN: </label><input type='text' name='code' required>"+
                "<input type='submit' value='Submit'>"+
                "</form><br><br>";
        message+="<a href='/login?usr=" + name + "' login='" + session + "'>HOME</a>";
        return message;
    }

    @RequestMapping("/login/ReturnProccess")
    public String ReturnProccess(@RequestParam("code") int IsbnCode, HttpSession session)
    {
        String name = (String) session.getAttribute("User");
        String message = "<html><body>";
        String ISBN = Integer.toString(IsbnCode);
        String InitialLine = null;
        String TargetLine = null;
        Boolean flag = false;
        for (String s:UsersToBooks.get(name))
        {
            if (s.equals(ISBN))
            {
                flag = true;
            }
        }

        if (flag) {
            for (Book t : books) {
                if (t.getIsbn() == IsbnCode) {
                    InitialLine = ISBN + " " + t.getFlag() + " " + t.getTitle() + " " + t.getCopies();
                    TargetLine = ISBN + " " + true + " " + t.getTitle() + " " + (t.getCopies() + 1);
                    //return the book means copy increment
                    books.add(new Book(IsbnCode, true, t.getTitle(), (t.getCopies() + 1)));
                    books.remove(t);
                    break;
                }
            }

            //update the AvailableBooks.txt
            String tmp = InitialLine;
            String tmp2 = TargetLine;
            try {
                Path path = Paths.get("src\\main\\java\\hello\\AvailableBooks.txt");
                Stream<String> lines = Files.lines(path);
                List<String> replaced = lines.map(line -> line.replaceAll(tmp, tmp2)).collect(Collectors.toList());
                Files.write(path, replaced);
                lines.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //update the existedusers.txt
            try {
                String line;
                String[] arr;
                File file = new File(EXISTED_USERS);
                File temp = File.createTempFile("file", ".txt", file.getParentFile());
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp)));
                while ((line = reader.readLine()) != null) {
                    arr = line.split(" ");
                    if (arr[0].equals(name)) {
                        line = line.replace(ISBN, "");
                        writer.println(line);
                    } else
                        writer.println(line);
                }
                reader.close();
                writer.close();
                file.delete();
                temp.renameTo(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UsersToBooks.remove(name, ISBN);

            message+="<h3>You succesfully returned the corresponding book. Thank you!</h3><br><br>";
        }

        else
            message+="<h3>Please correct the Isbn number</h3><br><br>";
        message+="<a href='/login?usr=" + name + "' login='" + session + "'>HOME</a><br>"+
                "<form action='/'><button type='submit' name = 'logout'>logout</button></form></body></html>";
        return message;
    }

    @RequestMapping("/login/UserBooks")
    public String UserBooks(HttpSession session)
    {
        String name = (String) session.getAttribute("User");
        String message = "<html><body><h4>YOUR LIST:</h4><br>";
        List<String> tmp = (List<String>) UsersToBooks.get(name);
        for (String s : tmp)
        {
            for(Book t : books)
            {
                if (Integer.toString(t.getIsbn()).equals(s))
                {
                    String BookTitle = t.getTitle();
                    message += "<i>"+BookTitle+"</i><br>";
                    break;
                }
            }
        }

        message+="<br><a href='/login?usr="+name+"'>HOME</a></body></html>";
        return message;
    }


    @RequestMapping("/login/BookList")
    public String BookList(HttpSession session)
    {
        String name = (String) session.getAttribute("User");
        int i;

        //print the list with underlined link for the existed books, and just a reference for the non existed books
        String message="<html>"+
                "<body>";
        for (i=0; i<books.size(); i++)
        {
            if(books.get(i).getFlag())
            {
                message += "<a href='/login/BookList/PrintBook?id="+i+"'>"+books.get(i).getTitle()+"</a><br/>";
            }
            else
            {
                message += books.get(i).getTitle()+"<br/>";
            }
        }
        message+="<br><br>"+"<a href='/login?usr="+name+"'> HOME</a>";
        message+="</body></html>";

        return (message);
    }

    @RequestMapping("/login/BookList/PrintBook")
    public String PrintBook(@RequestParam("id") String id,HttpSession session)
    {
        String name = (String) session.getAttribute("User");
        System.out.println(name);
        String message;
        int ID = Integer.parseInt(id);
        int isbn = books.get(ID).getIsbn();
        String booktitle = books.get((Integer.parseInt(id))).getTitle();

        message = "<html>"+
                "<body>"+
                "<h4>You selected the "+
                booktitle+
                " for rent.The isbn of your book is <b>"+Integer.toString(isbn)+".</b>"+"</br>"+"Do you want to continue with the rent process?</h4><br>"+
                "<a href='/login/BookList/PrintBook/RentProcess?id="+id+"'>"+"<button type='submit' name = 'submit'>Rent</button>"+"</a>"+
                "<a href='/login?usr="+name+"'><br><br>HOME</a>"+
                "</body></html>";
        return message;
    }

    @RequestMapping("/login/BookList/PrintBook/RentProcess")
    public String RentProcess(@RequestParam("id") String id,HttpSession session) {
        String name = (String) session.getAttribute("User");
        String message,TargetLine;
        int ID = Integer.parseInt(id);
        int isbn = books.get(ID).getIsbn();



        //check if the "name" has already this "isbn
        boolean flag = false;
        for (String s:UsersToBooks.get(name))
            if (Integer.toString(isbn).equals(s))
                flag = true; //He already has that book


        //point the isbn to the user in existedusers
        //Firstly, find the right line in the file

        if (!flag) {
            String InitialLine = books.get(ID).getIsbn() + " " + books.get(ID).getFlag() + " " + books.get(ID).getTitle() + " " + books.get(ID).getCopies();

            //Update the AvailableBooks.txt for the next sessions
            if (books.get(ID).getCopies() > 0)
                books.get(ID).setCopies(books.get(ID).getCopies() - 1);
            if (books.get(ID).getCopies() == 0) {
                TargetLine = books.get(ID).getIsbn() + " " + false + " " + books.get(ID).getTitle() + " " + books.get(ID).getCopies();
                books.get(ID).setFlag(false);
            }
            else
                TargetLine = books.get(ID).getIsbn() + " " + true + " " + books.get(ID).getTitle() + " " + books.get(ID).getCopies();

            try {
                Path path = Paths.get(AVAILABLE_BOOKS);
                Stream<String> lines = Files.lines(path);
                List<String> replaced = lines.map(line -> line.replaceAll(InitialLine, TargetLine)).collect(Collectors.toList());
                Files.write(path, replaced);
                lines.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String here = null;
            try {
                File f = new File(EXISTED_USERS);
                BufferedReader b = new BufferedReader(new FileReader(f));
                String[] arr;
                String line;
                while ((line = b.readLine()) != null) {
                    arr = line.split(" ");
                    if (arr[0].equals(name)) {
                        here = line;
                        break;
                    }
                }
                b.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            String here2 = here;
            //Secondly, do the change
            try {
                Path path = Paths.get(EXISTED_USERS);
                Stream<String> lines = Files.lines(path);
                List<String> replaced = lines.map(line -> line.replaceAll(here2, here2 + " " + Integer.toString(books.get(ID).getIsbn()))).collect(Collectors.toList());

                Files.write(path, replaced);
                lines.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //update the multimap
            UsersToBooks.put(name,Integer.toString(isbn));
            message = "<html><body><h4>Congrats!! Enjoy your book.</h4><br></body></html>";
        }
        else
            message = "<html><body><h4>You have already rent this book.</h4><br></body></html>";


        message += "<a href='/login?usr=" + name + "'><br><br>HOME</a></body></html>";
        return message;
    }

}

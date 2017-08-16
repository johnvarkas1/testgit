package hello;

public class Book
{
    private int isbn;
    private boolean flag;
    private String title;
    private int copies;

    public Book(int isbn, boolean flag, String title, int copies)
    {
        this.setIsbn(isbn);
        this.setFlag(flag);
        this.setTitle(title);
        this.setCopies(copies);
    }



    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public int getIsbn() {
        return isbn;
    }

    public void setIsbn(int isbn) {
        this.isbn = isbn;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return ("Title: "+this.getTitle()+
                "ISBN: "+ this.getIsbn());
    }
}

package Main;

public class PageId {
    private int FileIdx ;
    private int PageIdx;
    
    public PageId(int FileIdx, int PageIdx){
        this.FileIdx=FileIdx;
        this.PageIdx=PageIdx;
    }
    public int getFileIdx(){
        return FileIdx;
    }
    public int getPageIdx(){
        return PageIdx;
    }
    public String toString(){
        return "Indice de la page : (FileIdx: " + FileIdx + ", PageIdx:" + PageIdx + " )";
    }
    public  boolean equals(Object object){
        if (this==object){
            return true;
        }
        if(object==null|| getClass()!=object.getClass()){
            return false;
        }
        PageId NouvelPage =(PageId) object;
        System.out.println("On compare"+this+"avec"+ NouvelPage);
        return FileIdx == NouvelPage.FileIdx && PageIdx == NouvelPage.PageIdx;
    }
}

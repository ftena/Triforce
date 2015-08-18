package comm.tarlic.triforce;

import android.provider.BaseColumns;

class EnclosingDb{

public static abstract class Entry implements BaseColumns {
    public static final String TABLE_NAME = "table_timestamp";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";    
}

//Prevents the FeedReaderContract class from being instantiated.
private EnclosingDb() {}

}
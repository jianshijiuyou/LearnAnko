package info.jiuyou.learnanko.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * ==========================================
 * <p>
 * 版   权 ：jianshijiuyou(c) 2017
 * <br/>
 * 作   者 ：wq
 * <br/>
 * 版   本 ：1.0
 * <br/>
 * 创建日期 ：2017/7/7 0007  9:27
 * <br/>
 * 描   述 ：
 * <br/>
 * 修订历史 ：
 * </p>
 * ==========================================
 */
public class JavaDateBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "JavaDateBaseHelper";
    private String TABLE_NAME="User";


    public JavaDateBaseHelper(Context context) {
        super(context, "GitHub", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: create table");
        String sql = "create table if not exists " + TABLE_NAME + " (Id integer primary key, name text, email text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: 版本更新");
    }
}

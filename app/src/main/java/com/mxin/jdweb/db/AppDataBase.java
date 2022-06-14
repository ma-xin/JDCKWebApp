//package com.mxin.jdweb.db;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//import com.mxin.jdweb.App;
//import com.mxin.jdweb.db.dao.WebCookieDao;
//import com.mxin.jdweb.db.model.WebCookieModel;
//
//
//@Database(entities={WebCookieModel.class}
//    , version = 1)
//public abstract class AppDataBase extends RoomDatabase {
//
//    public static AppDataBase mAppDatabase;
//
//    public abstract WebCookieDao getWebCookieDao();
//
//
//    public static AppDataBase builder(){
//        if(mAppDatabase==null){
//            mAppDatabase = Room.databaseBuilder(App.getInstance(), AppDataBase.class, "web_data.db")
//                    .allowMainThreadQueries()
////                    .addMigrations(MIGRATION_1_2)
//                    .build();
//        }
//        return mAppDatabase;
//    }
//
//    /**
//     * 数据库版本 1->2 user表格新增了age列
//     */
////    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
////        @Override
////        public void migrate(SupportSQLiteDatabase database) {
////            database.execSQL("CREATE TABLE IF NOT EXISTS `rht_place` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `json` TEXT)");
////        }
////    };
//
//
//}

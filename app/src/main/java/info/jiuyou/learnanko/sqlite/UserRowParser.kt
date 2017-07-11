package info.jiuyou.learnanko.sqlite

import org.jetbrains.anko.db.MapRowParser

/**
 * ==========================================
 * <p>
 * 版   权 ：jianshijiuyou(c) 2017
 * <br/>
 * 作   者 ：wq
 * <br/>
 * 版   本 ：1.0
 * <br/>
 * 创建日期 ：2017/7/7 0007  15:12
 * <br/>
 * 描   述 ：
 * <br/>
 * 修订历史 ：
 * </p>
 * ==========================================
 */
class UserRowParser : MapRowParser<User> {
    override fun parseRow(columns: Map<String, Any?>): User {
        return User(columns["id"] as Long, columns["name"] as String, columns["email"] as String)
    }
}

# Note 8. 실전 안드로이드 앱: Room + Hilt + DataStore 통합 과정
> **강의 자료 재구성 버전 (중복 제거 및 실습 흐름 최적화)**
> 기존 PDF에서 Room 따로, DataStore 따로, Hilt가 중간에 섞여 있던 구조를 **하나의 통합된 앱을 빌드하는 자연스러운 순서**로 재배치했습니다.

---

## [Slide 1] 오늘 배울 내용
1. **로컬 저장소의 필요성**: 앱을 꺼도 데이터가 사라지지 않게 하려면?
2. **Room Database**: 할 일(Todo) 목록처럼 복잡한 데이터 저장하기
3. **DataStore**: 다크 모드, 알림 켜기 등 간단한 환경 설정 저장하기
4. **Hilt (의존성 주입)**: Room과 DataStore를 앱 전체에 쉽게 조립하기
5. **Jetpack Compose + Flow**: DB가 바뀌면 화면이 자동으로 바뀌는 마법!

---

## [Slide 2] 로컬 저장소: Room vs DataStore
안드로이드에서 데이터를 폰에 저장할 때, 목적에 따라 도구를 선택해야 합니다.

### Room (SQLite 기반 DB)
* **목적**: 앱의 "주요 데이터" (구조화된 대량 데이터)
* **특징**: SQL 쿼리 사용, 검색/정렬이 쉬움.
* **사용 예**: Todo 목록, 채팅 기록, 연락처

### DataStore (Preferences)
* **목적**: 앱의 "환경 설정" (단순 키-값 데이터)
* **특징**: 설정값(Key-Value)을 매우 빠르고 간단하게 저장.
* **사용 예**: 다크모드 On/Off, 알림 설정, 자동 로그인 여부

---

## [Slide 3] 프로젝트 준비하기 (build.gradle.kts)
가장 먼저, 오늘 사용할 Room, DataStore, Hilt 라이브러리를 추가합니다.

* 최근 안드로이드 트렌드인 **Version Catalog (`libs.`)** 방식과, Room/Hilt의 자동 코드 생성을 위한 **KAPT** 플러그인을 설정합니다.

```kotlin
plugins {
    alias(libs.plugins.hilt.android) // Hilt 플러그인
    id("kotlin-kapt")                // Room/Hilt용 코드 생성기
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
}
```

---

## [Slide 4] Step 1. Room으로 Todo DB 설계하기
데이터베이스를 만들려면 3가지가 필요합니다: **Entity, DAO, Database**

### 1. Entity (데이터 모델)
DB의 **테이블**입니다. `@Entity`를 붙여 만듭니다.
```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 자동 ID
    val title: String,
    val tag: String = "",
    val memo: String = "",
    val isDone: Boolean = false
)
```

---

## [Slide 5] Step 1. Room DAO와 Database
### 2. DAO (Data Access Object)
DB에 명령(CRUD)을 내리는 인터페이스입니다. `Flow`를 반환하면 데이터가 바뀔 때마다 실시간으로 알려줍니다!
```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    fun getAll(): Flow<List<TodoEntity>> // ✨ 데이터 실시간 감지!

    @Insert
    suspend fun insert(todo: TodoEntity) // DB 작업은 코루틴(suspend) 필수
}
```

### 3. Database
Room DB의 본체입니다.
```kotlin
@Database(entities = [TodoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
```

---

## [Slide 6] Step 2. DataStore로 환경설정(다크모드) 만들기
이번엔 Room보다 훨씬 가벼운 설정 저장소를 만듭니다.

```kotlin
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // 저장할 키 이름 설정
    private object SettingsKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    // 설정 읽기 (Flow로 실시간 감지)
    val darkModeFlow: Flow<Boolean> = dataStore.data.map { it[SettingsKeys.DARK_MODE] ?: false }

    // 설정 쓰기
    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { it[SettingsKeys.DARK_MODE] = value }
    }
}
```

---

## [Slide 7] Step 3. Hilt로 Room과 DataStore 조립하기 (의존성 주입)
우리가 만든 `AppDatabase`와 `DataStore`를 ViewModel에서 쓰려면, **Hilt**가 객체를 생성해서 넘겨줘야(주입) 합니다.

### Hilt 모듈 만들기 (@Provides)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // 1. Room DB 제공
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "todo_db").build()
    }

    // 2. DataStore 제공
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("settings") })
    }
}
```

---

## [Slide 8] Step 4. ViewModel 구성하기
ViewModel은 Hilt로부터 DB(Repository)를 주입받아, 화면(UI)과 데이터를 연결합니다.

```kotlin
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository // ✨ Hilt가 자동으로 넣어줌!
) : ViewModel() {

    // DAO에서 넘어온 Flow 데이터를 UI용 StateFlow로 변환
    val todoList = repository.allTodos.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun addTodo(todo: TodoEntity) {
        viewModelScope.launch { // 비동기로 DB에 Insert
            repository.insert(todo)
        }
    }
}
```

---

## [Slide 9] Step 5. UI 만들기 (Jetpack Compose)
이제 데이터를 화면에 그릴 차례입니다. `collectAsState()`를 쓰면 DB가 바뀔 때마다 화면이 자동으로 새로고침(리컴포지션) 됩니다!

```kotlin
@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    // ✨ DB 내용이 바뀌면 이 값이 자동으로 갱신됨!
    val todos by viewModel.todoList.collectAsState() 

    LazyColumn {
        items(todos) { todo ->
            TodoItem(todo = todo) // 리스트 그리기
        }
    }
}
```

---

## [Slide 10] 전체 데이터 흐름도 요약 (UDF)

앱이 완벽하게 돌아가는 핵심 원리는 **"단방향 데이터 흐름(UDF)"**과 **"Flow"**입니다.

1. **(UI)** 사용자가 앱에서 "추가" 버튼을 누름.
2. **(ViewModel)** `viewModel.addTodo()` 실행. 코루틴을 통해 비동기 작업 시작.
3. **(Repository -> Room)** Room DB 테이블에 새로운 Todo 데이터 Insert.
4. **(Flow 감지)** DB 내용이 변경되면, DAO의 `getAll(): Flow` 가 이를 감지하여 새로운 데이터 리스트를 밀어냄!
5. **(ViewModel -> UI)** ViewModel의 `StateFlow`가 업데이트 되고, Compose UI의 `collectAsState()`가 이를 받아 **화면을 자동으로 다시 그림(Recomposition)**.

> **결론**: 우리는 데이터를 강제로 새로고침 할 필요가 없습니다. Flow가 물 흐르듯 자동으로 화면을 바꿔줍니다!

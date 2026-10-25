# 📚 Note 8. 실전 안드로이드 앱 만들기 (Room, Hilt, DataStore)

> **강의 목표:** 기존에 메모리에만 저장되던 Todo 앱을 개선하여, **Room**으로 데이터를 영구 저장하고, **DataStore**로 앱 환경 설정(다크모드)을 관리하며, 이 모든 것을 **Hilt** 의존성 주입으로 깔끔하게 연결하는 모던 안드로이드 아키텍처를 배웁니다.

---

## 2~5쪽. 도입: 왜 로컬 저장소가 필요할까?

지금까지 만든 Todo 앱은 앱을 종료하면 모든 데이터가 사라집니다. (메모리에만 데이터를 유지하기 때문이죠.)
사용자의 데이터를 기기에 **영구적으로 저장**하려면 어떻게 해야 할까요?

안드로이드에서는 목적에 따라 두 가지 주요 로컬 저장소를 사용합니다.
1. **DataStore**: 키-값(Key-Value) 기반의 가벼운 설정 데이터. (예: 다크모드 On/Off, 알림 설정)
2. **Room (SQLite)**: 구조화된 대량의 데이터. (예: 할 일 목록, 연락처, 메모 등 CRUD가 필요한 데이터)

이번 수업에서는 이 두 가지를 모두 적용하여 앱을 종료해도 내용이 유지되는 완벽한 Todo 앱을 만들어 보겠습니다!

---

## 9쪽, 28쪽, 46쪽, 48쪽 종합. 프로젝트 설정 (build.gradle.kts)

가장 먼저 할 일은 필요한 라이브러리들을 가져오는 것입니다. 안드로이드 최신 표준인 **Version Catalog(`libs.versions.toml`)** 방식을 사용하여 Hilt, Room, DataStore 의존성을 한 번에 추가합니다.

### 📍 `build.gradle.kts` (App 수준)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Room 및 Hilt 코드 자동 생성을 위해 필수 (KSP 대안)
    alias(libs.plugins.hilt.android) // Hilt 플러그인 적용
}

dependencies {
    // 1. Room (로컬 DB)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler) // KAPT를 통한 Room 코드 생성

    // 2. Hilt (의존성 주입)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 3. DataStore (환경 설정 저장)
    implementation(libs.androidx.datastore.preferences)

    // 4. Navigation & ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

---

## 10~13쪽, 30쪽. 핵심 데이터베이스: Room DB 설계

Room은 크게 3가지 요소로 구성됩니다: **Entity(테이블), DAO(쿼리), Database(DB 인스턴스)**

### 1) Entity: 데이터 모델 정의 (`TodoEntity.kt`)
데이터베이스의 테이블 역할을 합니다.
```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 고유 ID 자동 생성
    val title: String,                                // 할 일 제목
    val tag: String = "",                             // 태그
    val memo: String = "",                            // 메모
    val description: String = "",                     // 설명
    val isDone: Boolean = false                       // 완료 여부
)
```

### 2) DAO: 데이터 접근 객체 (`TodoDao.kt`)
DB에 접근하여 데이터를 조작(CRUD)하는 메서드를 정의합니다.
*💡 핵심: `getAll()`의 반환 타입이 `Flow`입니다. DB가 변경되면 자동으로 UI에 새 데이터를 밀어줍니다(Reactive).*
```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    fun getAll(): Flow<List<TodoEntity>>

    @Insert
    suspend fun insert(todo: TodoEntity) // 시간이 걸릴 수 있으므로 suspend (코루틴) 사용

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)
}
```

### 3) AppDatabase (`AppDatabase.kt`)
```kotlin
@Database(entities = [TodoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    // (싱글톤 생성 로직은 Hilt 모듈에서 처리할 예정이므로 Companion 객체는 생략 가능합니다.)
}
```

---

## 43~47쪽, 51쪽. 환경 설정 관리: DataStore 적용

사용자가 다크 모드를 켰는지 여부는 Room처럼 무거운 DB에 넣을 필요가 없습니다. SharedPreferences를 대체하는 가벼운 `DataStore`를 사용합니다.

### 📍 `SettingsRepository.kt`
```kotlin
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // 키 정의
    private object SettingsKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATION = booleanPreferencesKey("notification")
    }

    // 읽기: Flow로 실시간 관찰
    val darkModeFlow: Flow<Boolean> = dataStore.data.map { it[SettingsKeys.DARK_MODE] ?: false }
    val notificationFlow: Flow<Boolean> = dataStore.data.map { it[SettingsKeys.NOTIFICATION] ?: true }

    // 쓰기: suspend 함수로 값 저장
    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { it[SettingsKeys.DARK_MODE] = value }
    }
    
    suspend fun setNotification(value: Boolean) {
        dataStore.edit { it[SettingsKeys.NOTIFICATION] = value }
    }
}
```

---

## 21~24쪽, 33쪽, 50쪽. 마법의 의존성 주입: Hilt 세팅

Room과 DataStore를 만들었지만, 이를 ViewModel에서 쓰려면 "생성해서 넘겨주어야" 합니다. 
Hilt를 사용하면 이 과정을 자동화할 수 있습니다!

### 1) 앱의 시작점 알리기 (`TodoApplication.kt` & `AndroidManifest.xml`)
```kotlin
@HiltAndroidApp
class TodoApplication : Application()
```
*(주의: 이 클래스를 만들고 반드시 `AndroidManifest.xml`의 `<application android:name=".TodoApplication">`으로 등록해야 합니다.)*

### 2) 객체 생성 방법 알려주기 (`AppModule.kt` & `DataStoreModule.kt`)
Hilt에게 Room DB와 DAO, 그리고 DataStore를 어떻게 만드는지 알려줍니다.

**`AppModule.kt` (Room 제공)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "todo_db").build()
    }

    @Provides
    fun provideTodoDao(database: AppDatabase): TodoDao = database.todoDao()
}
```

**`DataStoreModule.kt` (DataStore 제공)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> = 
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
}
```

---

## 14~16쪽, 31~32쪽, 56쪽. UI와 데이터의 연결: Repository & ViewModel

ViewModel은 Repository(데이터 계층)와 UI 사이의 징검다리입니다. `@HiltViewModel`을 붙이면 Hilt가 알아서 Repository를 주입해 줍니다.

### 📍 `TodoRepository.kt` & `TodoViewModel.kt`
```kotlin
@Singleton
class TodoRepository @Inject constructor(private val dao: TodoDao) {
    val allTodos = dao.getAll()
    suspend fun insert(todo: TodoEntity) = dao.insert(todo)
    suspend fun update(todo: TodoEntity) = dao.update(todo)
    suspend fun delete(todo: TodoEntity) = dao.delete(todo)
}

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository // Hilt가 자동 주입!
) : ViewModel() {

    // Flow 데이터를 StateFlow로 변환하여 Compose UI가 관찰할 수 있게 함
    val todoList = repository.allTodos.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun addTodo(todo: TodoEntity) {
        viewModelScope.launch { // DB 저장은 코루틴 비동기로 처리
            repository.insert(todo)
        }
    }
    // updateTodo, deleteTodo 생략
}
```

### 📍 `SettingsViewModel.kt`
```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {
    val darkMode = repo.darkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val notification = repo.notificationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun toggleDarkMode(v: Boolean) { viewModelScope.launch { repo.setDarkMode(v) } }
    fun toggleNotification(v: Boolean) { viewModelScope.launch { repo.setNotification(v) } }
}
```

---

## 34~41쪽, 54~58쪽. 대망의 Compose UI 연동 및 NavGraph

마지막으로 UI에서는 ViewModel의 데이터를 관찰(`collectAsState`)하여 화면에 그리고, Navigation Compose를 통해 화면을 이동합니다.

### 📍 `NavGraph.kt` (화면 이동 정의)
```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    val todoViewModel: TodoViewModel = hiltViewModel()
    
    NavHost(navController = navController, startDestination = "list") {
        composable("list") { TodoListScreen(todoViewModel, navController) }
        composable("addEdit?id={id}") { backStackEntry -> AddEditTodoScreen(todoViewModel, navController, backStackEntry) }
        composable("addEdit") { backStackEntry -> AddEditTodoScreen(todoViewModel, navController, backStackEntry) }
        composable("settings") { 
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(vm = settingsViewModel, onBack = { navController.popBackStack() }) 
        }
    }
}
```

### 📍 `TodoListScreen.kt` (할 일 목록 화면)
```kotlin
@Composable
fun TodoListScreen(viewModel: TodoViewModel, navController: NavController) {
    // StateFlow 관찰! DB가 바뀌면 이 리스트가 자동으로 갱신되고 리컴포지션 발생
    val todos by viewModel.todoList.collectAsState() 

    Scaffold(
        topBar = { TopBar(navController) }, // 설정 화면으로 가는 버튼 포함
        floatingActionButton = { 
            FloatingActionButton(onClick = { navController.navigate("addEdit") }) { Icon(Icons.Default.Add, "Add") } 
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(todos) { todo ->
                TodoItem(todo = todo, viewModel = viewModel, navController = navController)
            }
        }
    }
}
```

### 📍 `MainActivity.kt` (다크모드 앱 전체 적용)
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeVM: ThemeViewModel = hiltViewModel()
            val dark by themeVM.isDark.collectAsState() // DataStore 값 관찰
            
            MobileProjectTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
```

---

## 🎉 요약: 데이터 흐름 (18~19쪽)

이 앱의 데이터 흐름은 다음과 같습니다. 완벽한 **단방향 데이터 흐름(UDF)**과 **반응형 프로그래밍**을 보여줍니다.

1. **사용자 액션:** UI에서 "추가" 버튼 클릭
2. **ViewModel 호출:** `viewModel.addTodo(todo)` 실행 (코루틴 시작)
3. **DB 저장:** `Repository` → `DAO`를 거쳐 **Room DB에 저장**됨
4. **자동 감지:** Room DB 내용이 바뀌면 DAO의 `getAll() (Flow)`가 이를 감지하여 새 리스트 방출
5. **UI 갱신:** ViewModel의 `StateFlow`가 갱신되고, UI(`collectAsState`)가 자동으로 다시 그려짐 (리컴포지션)

이제 여러분의 Todo 앱은 폰을 껐다 켜도 데이터가 유지되고, 다크 모드 설정도 완벽하게 기억하는 진짜 앱이 되었습니다! 🚀
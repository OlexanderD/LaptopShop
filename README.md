# Alex Laptop Shop

Веб-додаток для обліку та контролю над базою даних магазину ноутбуків
-
Представлений додаток дозволяє переглядати записи, застосовуючи фільтрацію, вільно додавати та редагувати дані таблиць із забезпеченням консистентності даних. <br/>
Існує можливість безпечно експортувати/імпортувати таблиці до/із .xlsx файлів. <br/>
Реалізовано систему ролей, автентифікацію, реєстрацію та валідацію облікових записів через електронну пошту. 
Паролі користувачів шифруються за алгоритмом BCrypt. Кожен з контролерів захищений від звернень з боку користувачів, що не мають достатнього рівня доступу.
Тому всі несанкціоновані запити заслужено будуть отримувати лише помилку 403.
***

Додаток реалізовано на стеку:
-
- Back end: **Java 13 + Spring Boot (різноманітні вшиті сервіси)** <br/>
- Front end: **Freemarker + Bootstrap** <br/>
- Database: **Microsoft SQL Server** <br/>
***

Додаткові бібліотеки:
-
- _Jetbrains Annotations_
- _Apache POI_ - повна взаємодія з .xlsx файлами
- _Apache Commons Lang3_ - валідація даних під час імпортування з .xlsx файлів
- _Tomcat JSP Taglibs_ - теги для обмеження зображуваного контенту на веб-сторінці в залежності від ролі<br/>
Для керування залежностями використовується _Maven_
***

Демонстрація:
-
![AnonymousUser](/demoGifs/AnonymousDemo.gif)

![Registration](/demoGifs/RegistrationDemo.gif)

![CEOPanel](/demoGifs/CEODemo.gif)
***
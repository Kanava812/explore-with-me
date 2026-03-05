# EXPLORE WITH ME

## Описание

"Explore With Me" - приложение для организации досуга, с помощью которого можно организовывать мероприятия, искать интересующие события, принимать в них участие и тд. 

Проект построен по принципу микросервисной архитектуры, где каждая бизнес-функциональность вынесена в самостоятельный сервис с конкретной задачей, обеспечивая высокую масштабируемость и надежность системы.

---
## Стек технологий

- Язык и фреймворки: Java 21, Spring Boot 3
- Микросервисы: Spring Cloud (Config, Gateway, Eureka), Feign
- БД: PostgreSQL
- Очереди и потоковая обработка: Kafka
- Сериализация: Avro, Protobuf

---

## Архитектура

### Компоненты

- **Модуль infra**
    - **Gateway** (`gateway-server`) — маршрутизация запросов, единая точка входа для клиентов
      - **Service Discovery** (`discovery-server`) — реестр Eureka
    - **Config Server** (`config-server`) — конфигурации сервисов
- **Модуль core**
   - **Event Service** (`event-service`) —  создание, редактирование и просмотр событий, категорий, подборок и комментариев
   - **Request Service** (`request-service`) — работа с заявками
   - **User Service** (`user-service`) — регистрация, аутентификация и управление профилями пользователей
- **Модуль stats**
    - **Stats Server** (`stats-server`) — сбор аналитики и формирование персонализированных рекомендаций с использованием косинусного сходства. Интеграция с Apache Kafka обеспечивает асинхронную обработку потоков пользовательских событий.
- **Хранилища**
    - `event-db`, `request-db`, `user-db`, `analyzer-db`

### Взаимодействие

Сервисы взаимодействуют друг с другом через REST API с использованием ***Spring Cloud***

- Сервисы обнаруживаются автоматически через реестр **Eureka**, доступный по адресу `discovery-server`. 
- Маршрутизация запросов осуществляется через `gateway-server` с использованием механизма балансировки нагрузок *(lb://...)*.
- Конфигурационные файлы всех сервисов централизованно хранятся и доставляются из `config-server` через механизм обнаружения сервисов `spring.config.import=configserver:`.
Для межсерверного взаимодействия используется клиентская библиотека **Feign**, позволяющая обращаться к другим сервисам по их уникальным именам, зарегистрированным в реестре **Eureka**:

- `event-service` взаимодействует с:
   - `user-service`: получение информации об инициаторах событий
   - `request-service`: управление заявками на участие

- `request-service` взаимодействует с:
   - `event-service`: проверка событий и ограничений участия
   - `user-service`: проверка пользователей

### Конфигурации
1) ### **Основные настройки** (Файлы базовых настроек находятся непосредственно внутри каждого сервиса)
- `infra/gateway-server/src/main/resources/application.yaml`
- `infra/config-server/src/main/resources/application.yaml`
- `infra/discovery-server/src/main/resources/application.yaml`
- `core/*-service/src/main/resources/application.yaml`
- `stats/stats-server/src/main/resources/application.yaml`
            

2) ### **Центральные внешние настройки** (Для глобальных конфигураций используются файлы в репозитории `config-server`)
- `infra/config-server/src/main/resources/config/infra/gateway-server/application.yaml`
- `infra/config-server/src/main/resources/config/services/event-service/application.yaml`
- `infra/config-server/src/main/resources/config/services/request-service/application.yaml`
- `infra/config-server/src/main/resources/config/services/user-service/application.yaml`
- `infra/config-server/src/main/resources/config/stats/stats-server/application.yaml`

---
## **Внешний API**
API приложения доступен через API Gateway на порту 8080. Эндпоинты внешнего интерфейса классифицированы по типу доступа:
- Открытые (Public) — доступные любому пользователю без авторизации.
- Авторизированные (Private) — требуют аутентификации.
- Административные (Admin) — предназначены исключительно для администраторов

## **Внутренний API**
Клиенты внутреннего взаимодействия определены в модуле `core/interaction-api`. Методы реализуются контроллерами конкретных сервисов.


## Документация API
Спецификация API доступна в файлах:
- `ewm-main-service-spec.json` 
- `ewm-stats-service-spec.json` 

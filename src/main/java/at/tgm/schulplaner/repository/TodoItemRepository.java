/*
 * Copyright (c) 2020 tgm - Die Schule der Technik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.TodoItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TodoItemRepository extends ReactiveCrudRepository<TodoItem, UUID> {
    //@Query("SELECT * FROM todo_item WHERE todo_item.todo_list = :list")
    Flux<TodoItem> findAllByList(@NonNull UUID list);
}

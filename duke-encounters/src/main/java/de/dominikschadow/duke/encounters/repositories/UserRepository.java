/*
 * Copyright (C) 2016 Dominik Schadow, dominikschadow@gmail.com
 *
 * This file is part of the Application Intrusion Detection project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dominikschadow.duke.encounters.repositories;

import de.dominikschadow.duke.encounters.domain.DukeEncountersUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository class to access {@link DukeEncountersUser}s.
 *
 * @author Dominik Schadow
 */
public interface UserRepository extends JpaRepository<DukeEncountersUser, Long> {
    DukeEncountersUser findByUsername(String username);
}

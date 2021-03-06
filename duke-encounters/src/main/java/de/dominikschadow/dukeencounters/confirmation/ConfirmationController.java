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
package de.dominikschadow.dukeencounters.confirmation;

import de.dominikschadow.dukeencounters.encounter.EncounterService;
import de.dominikschadow.dukeencounters.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.appsensor.core.DetectionPoint;
import org.owasp.appsensor.core.DetectionSystem;
import org.owasp.appsensor.core.Event;
import org.owasp.appsensor.core.event.EventManager;
import org.owasp.security.logging.SecurityMarkers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.owasp.appsensor.core.DetectionPoint.Category.INPUT_VALIDATION;

/**
 * Controller to handle all encounter confirmation related requests.
 *
 * @author Dominik Schadow
 */
@Controller
@Slf4j
@AllArgsConstructor
public class ConfirmationController {
    private final ConfirmationService confirmationService;
    private final EncounterService encounterService;
    private final UserService userService;
    private final DetectionSystem detectionSystem;
    private final EventManager ids;

    @GetMapping("/confirmations")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String getConfirmations(final Model model,
                                   @RequestParam(name = "type", required = false) final String type) {
        List<Confirmation> confirmations = confirmationService.getConfirmations(type);
        model.addAttribute("confirmations", confirmations);

        return "user/confirmations";
    }

    @PostMapping("/confirmation/add")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ModelAndView addConfirmation(final long encounterId, final RedirectAttributes redirectAttributes) {
        String username = userService.getUsername();

        if (encounterService.isOwnEncounter(encounterId, username)) {
            log.info(SecurityMarkers.SECURITY_FAILURE, "User {} is owner of encounter {} and tried to confirm it",
                    username, encounterId);

            fireConfirmationErrorEvent();
            redirectAttributes.addFlashAttribute("ownEncounter", true);
        } else if (confirmationService.hasConfirmedEncounter(username, encounterId)) {
            log.info(SecurityMarkers.SECURITY_FAILURE, "User {} has already confirmed encounter {} and tried to "
                    + "confirm it again", username, encounterId);

            fireConfirmationErrorEvent();
            redirectAttributes.addFlashAttribute("secondConfirm", true);
        } else {
            confirmationService.addConfirmation(username, encounterId);

            log.info(SecurityMarkers.SECURITY_SUCCESS, "User {} confirmed encounter {}", username, encounterId);
        }

        return new ModelAndView("redirect:/account");
    }

    @PostMapping("/confirmation/revoke")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ModelAndView revokeConfirmation(final long confirmationId) {
        String username = userService.getUsername();

        confirmationService.deleteConfirmation(username, confirmationId);

        log.info(SecurityMarkers.SECURITY_SUCCESS, "User {} revoked confirmation {}", username, confirmationId);

        return new ModelAndView("redirect:/account");
    }

    private void fireConfirmationErrorEvent() {
        DetectionPoint detectionPoint = new DetectionPoint(INPUT_VALIDATION, "IE5-001");
        ids.addEvent(new Event(userService.getUser(), detectionPoint, detectionSystem));
    }
}

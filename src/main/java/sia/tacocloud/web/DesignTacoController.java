package sia.tacocloud.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Errors;
import sia.tacocloud.Ingredient;
import sia.tacocloud.Ingredient.Type;
import sia.tacocloud.Order;
import sia.tacocloud.Taco;
import sia.tacocloud.data.IngredientRepository;
import sia.tacocloud.data.TacoRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/design")
/*unlike the Taco object in the session, you need the order to be present across
multiple requests so that you can create multiple tacos and add them to the order.
The class-level @SessionAttributes annotation specifies any model objects like the
order attribute that should be kept in session and available across multiple requests.*/
@SessionAttributes("order")
public class DesignTacoController {
    private final IngredientRepository ingredientRepo;
    private TacoRepository designRepo;
    /*the @ModelAttribute annotation on order() ensures that an Order object will
    be created in the model.*/
    @ModelAttribute(name = "order")
    public Order order() {
        return new Order();
    }

    @ModelAttribute(name = "taco")
    public Taco taco() {
        return new Taco();
    }

    @Autowired
    public DesignTacoController(
            IngredientRepository ingredientRepo,
            TacoRepository designRepo) {
        this.ingredientRepo = ingredientRepo;
        this.designRepo = designRepo;
    }
    @GetMapping
    public String showDesignForm (Model model) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredientRepo.findAll().forEach(i -> ingredients.add(i));
        Type [] types = Type.values();
        for (Type type: types){
            model.addAttribute(type.toString().toLowerCase(),
                    filterByType(ingredients, type));
        }
        /*Модель - это объект, который передает данные между контроллером и любым представлением,
        отвечающим за рендеринг этих данных. В конечном итоге данные, помещенные в атрибуты модели,
        копируются в атрибуты ответа сервлета, где представление может их найти.*/
        model.addAttribute("design", new Taco());
        return "design"; /*the logical name of the view that will be used to render
        the model to the browser*/
    }

    @PostMapping
    public String processDesign(
            @Valid Taco design, Errors errors,
            /*The Order parameter is annotated with @ModelAttribute to indicate that its
            value should come from the model and that Spring MVC shouldn’t attempt to bind
            request parameters to it.*/
            @ModelAttribute Order order) {
        if (errors.hasErrors()) {
            return "design";
        }
        Taco saved = designRepo.save(design);
        order.addDesign(saved);
        return "redirect:/orders/current";
    }

    //tag::filterByType[]
    private List<Ingredient> filterByType(
            List<Ingredient> ingredients, Type type) {
        return ingredients
                .stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }
}

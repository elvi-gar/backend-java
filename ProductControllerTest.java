package lesson6;

import com.github.javafaker.Faker;
import lesson5.api.ProductService;
import lesson5.dto.Product;
import lesson5.utils.RetrofitUtils;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProductControllerTest {

    static ProductService productService;
    Product product = null;
    Product product2 = null;
    Faker faker = new Faker();
    static int id;
    static SqlSessionFactory sqlSessionFactory;
    static db.dao.ProductsMapper productsMapper;


    @BeforeAll
    static void beforeAll() throws IOException {
        productService = RetrofitUtils.getRetrofit().create(ProductService.class);
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }


    @BeforeEach
    void createProductTest() throws IOException {
        System.out.println("createProductTest");

        product = new Product()
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000));

        SqlSession session = sqlSessionFactory.openSession();
        productsMapper = session.getMapper(db.dao.ProductsMapper.class);
        db.model.Products products = new db.model.Products();


        Response<Product> response = productService.createProduct(product).execute();
        id = response.body().getId();

        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.code(), CoreMatchers.is(201));
        assertThat(response.body().getCategoryTitle(), CoreMatchers.is(product.getCategoryTitle()));
        assertThat(response.body().getTitle(), CoreMatchers.is(product.getTitle()));
        assertThat(response.body().getPrice(), CoreMatchers.is(product.getPrice()));
        assertThat(response.body().getId(), CoreMatchers.notNullValue());

        products.createCriteria().andIdEqualTo((long)id);
        List<db.model.Products> list = productsMapper.selectByExample(products);
        assertThat(list.get(0).getTitle(), CoreMatchers.is(product.getTitle()));
        assertThat(list.get(0).getPrice(), CoreMatchers.is(product.getPrice()));
        assertThat(list.get(0).getCategory_id(), CoreMatchers.is(1L));
        assertThat(list.get(0).getId(), CoreMatchers.notNullValue());
    }


    @Test
    void getProductByIdTest() throws IOException {
        System.out.println("getProductByIdTest");
        Response<Product> response = productService.getProductById(id).execute();

        SqlSession session = sqlSessionFactory.openSession();
        productsMapper = session.getMapper(db.dao.ProductsMapper.class);
        db.model.Products products = new db.model.Products();

        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.code(), CoreMatchers.is(200));
        assertThat(response.body().getCategoryTitle(), CoreMatchers.is(product.getCategoryTitle()));
        assertThat(response.body().getTitle(), CoreMatchers.is(product.getTitle()));
        assertThat(response.body().getPrice(), CoreMatchers.is(product.getPrice()));
        assertThat(response.body().getId(), CoreMatchers.is(id));

        products.createCriteria().andIdEqualTo((long)id);
        List<db.model.Products> list = productsMapper.selectByExample(products);
        assertThat(list.get(0).getTitle(), CoreMatchers.is(product.getTitle()));
        assertThat(list.get(0).getPrice(), CoreMatchers.is(product.getPrice()));
        assertThat(list.get(0).getId(), CoreMatchers.is((long) id));
    }


    @Test
    void modifyProductTest() throws IOException {
        System.out.println("modifyProductTest");

        product2 = new Product()
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000))
                .withId(id);

        SqlSession session = sqlSessionFactory.openSession();
        productsMapper = session.getMapper(db.dao.ProductsMapper.class);
        db.model.Products products2 = new db.model.Products();

        Response<Product> response = productService.modifyProduct(product2).execute();

        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.code(), CoreMatchers.is(200));
        assertThat(response.body().getCategoryTitle(), CoreMatchers.is(product2.getCategoryTitle()));
        assertThat(response.body().getTitle(), CoreMatchers.is(product2.getTitle()));
        assertThat(response.body().getPrice(), CoreMatchers.is(product2.getPrice()));
        assertThat(response.body().getId(), CoreMatchers.is(id));

        products2.createCriteria().andIdEqualTo((long)id);
        List<db.model.Products> list2 = productsMapper.selectByExample(products2);
        assertThat(list2.get(0).getTitle(), CoreMatchers.is(product2.getTitle()));
        assertThat(list2.get(0).getPrice(), CoreMatchers.is(product2.getPrice()));
        assertThat(list2.get(0).getId(), CoreMatchers.is((long) id));
    }


    @Test
    void getProductsTest() throws IOException {
        System.out.println("getProductsTest");
        Response<ResponseBody> response = productService.getProducts().execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
        assertThat(response.code(), CoreMatchers.is(500));
    }


    @SneakyThrows
    @AfterAll
    static void tearDown() {
        System.out.println("deleteProduct");
        Response<ResponseBody> response = productService.deleteProduct(id).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.code(), CoreMatchers.is(200));

        SqlSession session = sqlSessionFactory.openSession();
        productsMapper = session.getMapper(db.dao.ProductsMapper.class);
        db.model.Products products = new db.model.Products();

        products.createCriteria().andIdEqualTo((long)id);
        assertThat(productsMapper.countByExample(products), CoreMatchers.is(0L));
    }
}

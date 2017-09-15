import RxRest, {jsonInterceptor, errorInterceptor} from 'ts-rx-rest';

export const Rest = new RxRest()
    .wrapRequest(r => {
        r.withCredentials = false;
        return r;
    })
    .wrap(errorInterceptor)
    .wrap(jsonInterceptor);
